package ace.charitan.payment.external.service;

import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import com.stripe.exception.StripeException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ExternalPaymentService {
    List<String> cancelStripeSubscriptionForHaltProject(String projectId) throws StripeException;
    String createPaymentRedirectUrl(CreatePaymentIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException;
}
