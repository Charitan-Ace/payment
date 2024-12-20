package ace.charitan.payment.controller;

import ace.charitan.payment.internal.service.InternalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Autowired
    private InternalPaymentService service;
}
