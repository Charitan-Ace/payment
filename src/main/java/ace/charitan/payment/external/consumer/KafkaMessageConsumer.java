package ace.charitan.payment.external.consumer;


import ace.charitan.common.dto.payment.CancelHaltedProjectSubscriptionRequestDto;
import ace.charitan.common.dto.payment.CancelHaltedProjectSubscriptionResponseDto;
import ace.charitan.common.dto.payment.CreateDonationPaymentRedirectUrlRequestDto;
import ace.charitan.common.dto.payment.CreateDonationPaymentRedirectUrlResponseDto;
import ace.charitan.payment.external.service.ExternalPaymentService;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaMessageConsumer {
    @Autowired
    private ExternalPaymentService service;

    @KafkaListener(topics = "payment.halt-project-subscriptions")
    @SendTo
    public CancelHaltedProjectSubscriptionResponseDto cancelSubscriptionsForHaltProject(CancelHaltedProjectSubscriptionRequestDto dto) throws StripeException {
        return new CancelHaltedProjectSubscriptionResponseDto(service.cancelStripeSubscriptionForHaltProject(dto.getProjectId()));
    }

    @KafkaListener(topics = "payment.create-payment-redirect-url")
    @SendTo
    public CreateDonationPaymentRedirectUrlResponseDto createPaymentRedirectUrl(CreateDonationPaymentRedirectUrlRequestDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String redirectUrl = service.createPaymentRedirectUrl(new CreatePaymentIntentDto(dto.getUserId(), dto.getDonationId(), dto.getAmount(), "usd", dto.getSuccessUrl(), dto.getCancelUrl()));
        return new CreateDonationPaymentRedirectUrlResponseDto(redirectUrl);
    }

}

