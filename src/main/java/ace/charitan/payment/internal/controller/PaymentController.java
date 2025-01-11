package ace.charitan.payment.internal.controller;

import ace.charitan.payment.internal.dto.*;
import ace.charitan.payment.internal.service.InternalPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
class PaymentController {

    @Autowired
    private InternalPaymentService service;


    @PostMapping("/donation-checkout")
    public ResponseEntity<StripeRedirectUrlResponseDto> createPaymentIntent(@RequestBody CreatePaymentIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String redirectUrl = service.createPaymentRedirectUrl(dto);
        return ResponseEntity.ok(new StripeRedirectUrlResponseDto(redirectUrl));
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@RequestBody CreateCustomerDto dto) throws StripeException {
        String customerId = service.createCustomer(dto);
        return ResponseEntity.ok(customerId);
    }

    @PostMapping("/card-setup")
    public ResponseEntity<StripeRedirectUrlResponseDto> createSetupIntent(@RequestBody CreateSetupIntentDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String redirectUrl = service.createSetupIntentRedirectUrl(dto);
        return ResponseEntity.ok(new StripeRedirectUrlResponseDto(redirectUrl));
    }

    @PostMapping("/monthly-subscription")
    public ResponseEntity<StripeRedirectUrlResponseDto> createSubscription(@RequestBody CreateSubscriptionDto dto) throws StripeException, AccessDeniedException, ExecutionException, InterruptedException {
        String redirectUrl = service.createSubscriptionRedirectUrl(dto);
        return ResponseEntity.ok(new StripeRedirectUrlResponseDto(redirectUrl));
    }


    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        String endpointSecret = System.getenv("STRIPE_WEBHOOK_SECRET");
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            service.handleStripeWebhookEvent(event);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok("Webhook received");
    }
}
