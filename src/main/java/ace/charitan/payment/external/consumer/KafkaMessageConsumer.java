package ace.charitan.payment.external.consumer;


import ace.charitan.common.dto.payment.CancelHaltedProjectSubscriptionRequestDto;
import ace.charitan.payment.external.service.ExternalPaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageConsumer {
    @Autowired
    private ExternalPaymentService service;

    @KafkaListener(topics = "payment.halt-project-subscriptions")
    public void cancelSubscriptionsForHaltProject(CancelHaltedProjectSubscriptionRequestDto dto) throws StripeException {
        service.cancelStripeSubscriptionForHaltProject(dto.getProjectId());
    }
}

