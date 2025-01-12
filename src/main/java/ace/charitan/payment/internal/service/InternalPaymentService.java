package ace.charitan.payment.internal.service;

import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import ace.charitan.payment.internal.dto.CreateSubscriptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface InternalPaymentService {
    String createCustomer(CreateCustomerDto dto) throws StripeException;
    String createSetupIntentRedirectUrl(CreateSetupIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException;
    String createPaymentRedirectUrl(CreatePaymentIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException;
    String createSubscriptionRedirectUrl(CreateSubscriptionDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException;
    void handleStripeWebhookEvent(Event event) throws JsonProcessingException;
    void cancelStripeSubscriptionForHaltProject(String projectId) throws StripeException;
    Boolean cancelStripeSubscription(String projectId) throws StripeException;
}
