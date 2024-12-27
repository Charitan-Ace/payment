package ace.charitan.payment.internal.service;

import ace.charitan.payment.internal.dto.ConfirmPaymentIntentDto;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;

public interface InternalPaymentService {
    String createPaymentIntent(CreatePaymentIntentDto dto) throws StripeException;
    String createCustomer(CreateCustomerDto dto) throws StripeException;
    Customer getCustomer(String id) throws StripeException;
    PaymentIntent confirmPaymentIntent(ConfirmPaymentIntentDto dto) throws StripeException;
    String createSetupIntent(CreateSetupIntentDto dto) throws StripeException;
}
