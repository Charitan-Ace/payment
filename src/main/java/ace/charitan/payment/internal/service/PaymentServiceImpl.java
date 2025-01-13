package ace.charitan.payment.internal.service;

import ace.charitan.common.dto.email.payment.EmailPaymentHaltedProjectCancelSubscriptionEmailDto;
import ace.charitan.common.dto.profile.donor.DonorProfileDto;
import ace.charitan.common.dto.profile.donor.DonorsDto;
import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.auth.AuthModel;
import ace.charitan.payment.internal.auth.AuthUtils;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import ace.charitan.payment.internal.dto.CreateSubscriptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
class PaymentServiceImpl implements InternalPaymentService, ExternalPaymentService {

    @Autowired
    private KafkaMessageProducer producer;

    @Override
    public String createCustomer(CreateCustomerDto dto) throws StripeException {
        CustomerCreateParams params = new CustomerCreateParams.Builder()
                .setName(dto.getName())
                .setEmail(dto.getEmail())
                .build();
        return Customer.create(params).getId();
    }

    @Override
    public String createSetupIntentRedirectUrl(CreateSetupIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String stripeCustomerId = getStripeCustomerIdFromContext();

        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripeCustomerId);
        params.put("payment_method_types", List.of("card"));
        params.put("mode", "setup");
        params.put("success_url", dto.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}");
        params.put("cancel_url", dto.getCancelUrl());

        Session session = Session.create(params);

        return session.getUrl();
    }

    @Override
    public String createPaymentRedirectUrl(CreatePaymentIntentDto dto) throws StripeException, ExecutionException, InterruptedException {
        String stripeCustomerId = getStripeCustomerIdFromUserId(dto.getUserId());

        Long price = (long) (dto.getAmount() * 100);

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Donation")
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(dto.getCurrency())
                        .setUnitAmount(price)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(1L)
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(lineItem)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(dto.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(dto.getCancelUrl())
                .putMetadata("donationId", String.valueOf(dto.getDonationId()))
                .build();

        Session session = Session.create(params);

        return session.getUrl();
    }

    public List<PaymentMethod> getPaymentMethods() throws AccessDeniedException, ExecutionException, InterruptedException {
        String stripeCustomerId = getStripeCustomerIdFromContext();

        try {
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            return PaymentMethod.list(params).getData();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to list payment methods", e);
        }
    }

    @Override
    public String createSubscriptionRedirectUrl(CreateSubscriptionDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String stripeCustomerId = getStripeCustomerIdFromContext();

        AuthModel authModel = AuthUtils.getUserDetails();
        if (authModel != null) {
            String userId = authModel.getUsername();
            SubscriptionSearchParams params = SubscriptionSearchParams.builder()
                    .setQuery(String.format("status:'active' AND metadata['projectId']: '%s' AND metadata['donorId']: '%s'", dto.getProjectId(), userId))
                    .setLimit(100L)
                    .build();
            List<Subscription> subscriptionList = Subscription.search(params).getData();
            if (!subscriptionList.isEmpty()) {
                System.out.println(subscriptionList);
                throw new RuntimeException("You already has a monthly donation to this project");
            }
        }
        Long priceLong = (long) (dto.getAmount() * 100);

        PriceCreateParams priceCreateParams = PriceCreateParams.builder()
                .setCurrency("usd")
                .setUnitAmount(priceLong)
                .setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                                .build()
                )
                .setProductData(
                        PriceCreateParams.ProductData.builder()
                                .setName("Monthly Donation")
                                .build()
                )
                .build();

        Price price = Price.create(priceCreateParams);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("projectId", dto.getProjectId());
        metadata.put("amount", String.valueOf(dto.getAmount()));
        metadata.put("message", "Monthly donation for project " + dto.getProjectId());
        if (authModel != null) {
            String userId = authModel.getUsername();
            metadata.put("donorId", userId);
        }

        SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
                .setBillingCycleAnchor(getNextBillingCycleAnchor())
                .putAllMetadata(metadata)
                .build();


        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(price.getId())
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(dto.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(dto.getCancelUrl())
                .setSubscriptionData(subscriptionData)
                .build();

        Session session = Session.create(params);

        return session.getUrl();
    }

    @Override
    public void handleStripeWebhookEvent(Event event) throws JsonProcessingException {
        System.out.println("Webhook triggered: " + event.getType());
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "invoice.payment_succeeded":
                handleSubscriptionChargeCompleted(event);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
                break;
        }

    }

    private void handleCheckoutSessionCompleted(Event event) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();

                Map<String, String> metadata = session.getMetadata();
                String donationId = metadata.get("donationId");

                producer.updateDonationStripeId(Long.parseLong(donationId), session.getPaymentIntent());
            } else {
                throw new IllegalArgumentException("Failed to deserialize session data");
            }
    }

    private void handleSubscriptionChargeCompleted(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            Invoice invoice = (Invoice) deserializer.getObject().get();

            String subscriptionId = invoice.getSubscription();

            if (subscriptionId != null) {
                try {
                    Subscription subscription = Subscription.retrieve(subscriptionId);

                    Map<String, String> subscriptionMetadata = subscription.getMetadata();
                    System.out.println("Subscription Metadata: " + subscriptionMetadata);

                    String projectId = subscriptionMetadata.get("projectId");
                    String donorId = subscriptionMetadata.get("donorId");
                    Double amount = Double.parseDouble(subscriptionMetadata.get("amount"));
                    String message = subscriptionMetadata.get("message");

                    producer.createMonthlyDonation(amount, message, invoice.getPaymentIntent(), projectId, donorId);

                } catch (StripeException e) {
                    System.err.println("Failed to fetch subscription details: " + e.getMessage());
                }
            }

        } else {
            throw new IllegalArgumentException("Failed to deserialize session data");
        }

    }

    private String getStripeCustomerIdFromContext() throws ExecutionException, InterruptedException, AccessDeniedException {
        AuthModel authModel = AuthUtils.getUserDetails();
        if (authModel != null) {
            String userId = authModel.getUsername();

            DonorsDto donorsDto = producer.getDonorProfilesById(List.of(UUID.fromString(userId)));
            System.out.println(donorsDto.donorProfilesList().size());
            donorsDto.donorProfilesList().forEach(System.out::println);
            DonorProfileDto profile = donorsDto.donorProfilesList().getFirst();
            return profile.stripeId();
        } else {
            throw new AccessDeniedException("You must login to use this function");
        }
    }

    private String getStripeCustomerIdFromUserId(String userId) throws ExecutionException, InterruptedException {
        DonorsDto donorsDto = producer.getDonorProfilesById(List.of(UUID.fromString(userId)));
        DonorProfileDto profile = donorsDto.donorProfilesList().getFirst();
        return profile.stripeId();
    }

    private long getNextBillingCycleAnchor() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next15th = now.getDayOfMonth() > 15
                ? now.plusMonths(1).withDayOfMonth(15)
                : now.withDayOfMonth(15);
        return next15th.toEpochSecond(ZoneOffset.UTC);
    }

    public List<String> getSubscribedProjects() throws StripeException {
        AuthModel authModel = AuthUtils.getUserDetails();
        if (authModel != null) {
            String userId = authModel.getUsername();
            SubscriptionSearchParams params = SubscriptionSearchParams.builder()
                    .setQuery(String.format("status:'active' AND metadata['donorId']: '%s'", userId))
                    .setLimit(100L)
                    .build();

            List<Subscription> subscriptions = Subscription.search(params).getData();

            return subscriptions.stream().map(
                    subscription -> {
                        Map<String, String> metadata = subscription.getMetadata();
                        return metadata.get("projectId");
                    }).toList();

        } else {
            throw new RuntimeException("Unauthorized");
        }
    }

    public Boolean cancelStripeSubscription(String projectId) throws StripeException {
        AuthModel authModel = AuthUtils.getUserDetails();
        if (authModel != null) {
            String userId = authModel.getUsername();
            SubscriptionSearchParams params = SubscriptionSearchParams.builder()
                    .setQuery(String.format("status:'active' AND metadata['projectId']: '%s' AND metadata['donorId']: '%s'", projectId, userId))
                    .setLimit(100L)
                    .build();

            cancelSubscriptions(params);
            return true;

        } else {
            throw new RuntimeException("No auth model");
        }
    }

    public List<String> cancelStripeSubscriptionForHaltProject(String projectId) throws StripeException {
        SubscriptionSearchParams params = SubscriptionSearchParams.builder()
                .setQuery(String.format("status:'active' AND metadata['projectId']: '%s'", projectId))
                .setLimit(100L)
                .build();
        List<Subscription> subscriptions = cancelSubscriptions(params);
        return subscriptions.stream().map(
                subscription -> {
                    Map<String, String> metadata = subscription.getMetadata();
                    return metadata.get("donorId");
                }).toList();
//        donorIds.forEach(donorId -> producer.sendCancelSubscriptionEmail(new EmailPaymentHaltedProjectCancelSubscriptionEmailDto(donorId, "Monthly subscription cancellation", "Monthly donation subscription for project " + projectId + " is cancelled.")));
    }

    private List<Subscription> cancelSubscriptions(SubscriptionSearchParams params) throws StripeException {
        List<Subscription> subscriptions = Subscription.search(params).getData();

        return subscriptions.parallelStream()
                .map(subscription -> {
                    try {
                        return subscription.cancel();
                    } catch (Exception e) {
                        throw new RuntimeException("Exception while canceling subscription", e);
                    }
                }).toList();
    }



}
