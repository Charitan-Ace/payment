package ace.charitan.payment.internal.service;

import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSubscriptionDto;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
    public Customer getCustomer(String id) throws StripeException {
        return Customer.retrieve(id);
    }

    @Override
    public String createSetupIntent() throws StripeException {
        String stripeCustomerId = getStripeCustomerIdFromProfileService();
        Map<String, Object> params = new HashMap<>();
        params.put("payment_method_types", new String[]{"card"});
        SetupIntent intent = SetupIntent.create(params);

        return intent.getClientSecret();
    }

    @Override
    public PaymentIntent createPaymentIntent(CreatePaymentIntentDto dto) throws StripeException {
        String stripeCustomerId = getStripeCustomerIdFromProfileService();

        PaymentIntentCreateParams.Builder paramsBuilder = new PaymentIntentCreateParams.Builder()
                .setAmount(dto.getAmount())
                .setCurrency(dto.getCurrency())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .setEnabled(true)
                                .build())
                .setCustomer(stripeCustomerId);

        PaymentIntentCreateParams params = paramsBuilder.build();
        PaymentIntent intent = PaymentIntent.create(params);

        producer.updateDonationStripeId(dto.getDonationId(), intent.getId());

        return intent;
    }

    public List<PaymentMethod> getPaymentMethods() {
        String stripeCustomerId = getStripeCustomerIdFromProfileService();

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

    public Subscription createSubscription(CreateSubscriptionDto dto) throws StripeException {
        PriceCreateParams priceCreateParams =
              PriceCreateParams.builder()
                .setCurrency("usd")
                .setUnitAmount(dto.getAmount())
                .setRecurring(
                  PriceCreateParams.Recurring.builder()
                    .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                    .build()
                )
                .setProductData(
                  PriceCreateParams.ProductData.builder().setName("Monthly Donation").build()
                )
        .build();
        Price price = Price.create(priceCreateParams);
        SubscriptionCreateParams params =
              SubscriptionCreateParams.builder()
                .setCustomer(getStripeCustomerIdFromProfileService())
                .addItem(
                  SubscriptionCreateParams.Item.builder()
                    .setPrice(price.getId())
                    .build()
                )
                      .setBillingCycleAnchor(getNextBillingCycleAnchor())
                .build();
        return Subscription.create(params);
    }

    private String getStripeCustomerIdFromProfileService() {
      //TODO: GET STRIPE CUSTOMER ID FROM PROFILE SERVICE HERE
        return "cus_RTvFt09w0eed0B";
    }

    private long getNextBillingCycleAnchor() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next15th = now.getDayOfMonth() > 15
                ? now.plusMonths(1).withDayOfMonth(15)
                : now.withDayOfMonth(15);
        return next15th.toEpochSecond(ZoneOffset.UTC);
    }

}
