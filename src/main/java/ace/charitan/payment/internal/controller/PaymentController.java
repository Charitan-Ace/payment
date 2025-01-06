package ace.charitan.payment.internal.controller;

import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import ace.charitan.payment.internal.service.InternalPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
class PaymentController {

    @Autowired
    private InternalPaymentService service;


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody CreatePaymentIntentDto dto) throws StripeException {
        PaymentIntent intent = service.createPaymentIntent(dto);

        // Create a simplified response
        Map<String, Object> response = new HashMap<>();
        response.put("id", intent.getId());
        response.put("client_secret", intent.getClientSecret());
        response.put("amount", intent.getAmount());
        response.put("currency", intent.getCurrency());
        response.put("status", intent.getStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@RequestBody CreateCustomerDto dto) throws StripeException {
        String customerId = service.createCustomer(dto);
        return ResponseEntity.ok(customerId);
    }

    @PostMapping("/create-setup-intent")
    public ResponseEntity<String> createSetupIntent(@RequestBody CreateSetupIntentDto dto) throws StripeException {
        String clientSecret = service.createSetupIntent(dto);
        return ResponseEntity.ok(clientSecret);
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(@RequestParam String stripeCustomerId) throws StripeException {
        List<PaymentMethod> paymentMethods = service.getPaymentMethods(stripeCustomerId);
        return ResponseEntity.ok(paymentMethods);
    }

}
