package ace.charitan.payment.internal.service;

import ace.charitan.payment.internal.dto.ChargeRequestDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

public interface InternalPaymentService {
    Charge charge(ChargeRequestDto dto) throws StripeException;
}
