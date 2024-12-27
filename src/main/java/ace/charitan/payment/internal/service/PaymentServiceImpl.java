package ace.charitan.payment.internal.service;

import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.dto.ConfirmPaymentIntentDto;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class PaymentServiceImpl implements InternalPaymentService, ExternalPaymentService {

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
        Map<String, Object> params = new HashMap<>();
        params.put("customer", dto.getCustomerId());
        params.put("payment_method_types", new String[]{"card"});
        SetupIntent intent = SetupIntent.create(params);

        return intent.getClientSecret();
    }

    @Override
    public String createPaymentIntent(CreatePaymentIntentDto dto) throws StripeException {
        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
                .setAmount(dto.getAmount())
                .setCurrency(dto.getCurrency())
                .setCustomer(dto.getStripeCustomerId())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .setEnabled(true)
                            .build()
                )
//                .setReturnUrl("http://localhost:8080/something")
                .build();
        return PaymentIntent.create(params).getClientSecret();
    }

    @Override
    public PaymentIntent confirmPaymentIntent(ConfirmPaymentIntentDto dto) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(dto.getIntentId());
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(dto.getPaymentMethod())
                .build();
        intent.confirm(params);
        return intent;
    }
}
