package ace.charitan.payment.internal.service;

import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;

import java.util.List;

public interface InternalPaymentService {
    PaymentIntent createPaymentIntent(CreatePaymentIntentDto dto) throws StripeException;
    String createCustomer(CreateCustomerDto dto) throws StripeException;
    Customer getCustomer(String id) throws StripeException;
    String createSetupIntent(CreateSetupIntentDto dto) throws StripeException;
    List<PaymentMethod> getPaymentMethods(String stripeCustomerId) throws StripeException;
}
