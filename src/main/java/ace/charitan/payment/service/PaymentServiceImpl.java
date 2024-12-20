package ace.charitan.payment.service;

import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.dto.ChargeRequestDto;
import ace.charitan.payment.internal.service.InternalPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class PaymentServiceImpl implements InternalPaymentService, ExternalPaymentService {

    @Override
    public Charge charge(ChargeRequestDto dto) throws StripeException {
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", dto.getAmount());
        chargeParams.put("currency", dto.getCurrency());
        chargeParams.put("description", dto.getDescription());
        chargeParams.put("source", dto.getStripeToken());
        return Charge.create(chargeParams);
    }
}
