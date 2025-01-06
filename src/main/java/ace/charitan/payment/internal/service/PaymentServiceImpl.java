package ace.charitan.payment.internal.service;

import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodListParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public String createSetupIntent(CreateSetupIntentDto dto) throws StripeException {
//        Optional<String> stripeCustomerId = getStripeCustomerIdFromProfileService();
        Map<String, Object> params = new HashMap<>();
        if (dto.getCustomerId() != null) {
            params.put("customer", dto.getCustomerId());
        }
//        stripeCustomerId.ifPresent(s -> params.put("customer", s));
        params.put("payment_method_types", new String[]{"card"});
        SetupIntent intent = SetupIntent.create(params);

        return intent.getClientSecret();
    }

    @Override
    public PaymentIntent createPaymentIntent(CreatePaymentIntentDto dto) throws StripeException {
        PaymentIntentCreateParams.Builder paramsBuilder = new PaymentIntentCreateParams.Builder()
                .setAmount(dto.getAmount())
                .setCurrency(dto.getCurrency())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .setEnabled(true)
                                .build()
                );

        String stripeCustomerId = dto.getCustomerId();

        if (stripeCustomerId != null) {
            paramsBuilder.setCustomer(stripeCustomerId);
        }

        PaymentIntentCreateParams params = paramsBuilder.build();
        PaymentIntent intent = PaymentIntent.create(params);
        System.out.println(dto);

        producer.updateDonationStripeId(dto.getDonationId(), intent.getId());

        return intent;
    }

    public List<PaymentMethod> getPaymentMethods(String stripeCustomerId) {
//        Optional<String> stripeCustomerId = getStripeCustomerIdFromProfileService();
        if (stripeCustomerId.isEmpty()) {
            return Collections.emptyList(); // No Stripe customer linked
        }

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

    private Optional<String> getStripeCustomerIdFromProfileService() {
        //TODO: GET STRIPE CUSTOMER ID FROM PROFILE SERVICE HERE
//        return Optional.of("cus_RTvFt09w0eed0B");
        return Optional.empty();
    }



}
