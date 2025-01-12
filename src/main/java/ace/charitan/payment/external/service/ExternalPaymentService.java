package ace.charitan.payment.external.service;

import com.stripe.exception.StripeException;

public interface ExternalPaymentService {
    void cancelStripeSubscriptionForHaltProject(String projectId) throws StripeException;
}
