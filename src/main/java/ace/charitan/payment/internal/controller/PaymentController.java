package ace.charitan.payment.internal.controller;

import ace.charitan.payment.internal.dto.ConfirmPaymentIntentDto;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.dto.CreateSetupIntentDto;
import ace.charitan.payment.internal.service.InternalPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
class PaymentController {

    @Autowired
    private InternalPaymentService service;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody CreatePaymentIntentDto dto) throws StripeException {
        String clientSecret = service.createPaymentIntent(dto);
        return ResponseEntity.ok(clientSecret);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@RequestBody CreateCustomerDto dto) throws StripeException {
        String customerId = service.createCustomer(dto);
        return ResponseEntity.ok(customerId);
    }

//    @GetMapping("/customer")
//    public ResponseEntity<Map<String, Object>> getCustomer(@RequestParam String id) throws StripeException {
//        Customer customer = service.getCustomer(id);
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonString = customer.toJson();
//            Map<String, Object> responseMap = mapper.readValue(jsonString, Map.class);
//            return ResponseEntity.ok(responseMap);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "Failed to process customer data"));
//        }
//    }

    @PutMapping("/confirm-payment-intent")
    public ResponseEntity<Map<String, Object>> confirmPaymentIntent(@RequestBody ConfirmPaymentIntentDto dto) throws StripeException {
        PaymentIntent intent = service.confirmPaymentIntent(dto);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = intent.toJson();
            Map<String, Object> responseMap = mapper.readValue(jsonString, Map.class);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process customer data"));
        }
    }

    @PostMapping("/create-setup-intent")
    public ResponseEntity<String> createSetupIntent(@RequestBody CreateSetupIntentDto dto) throws StripeException {
        String clientSecret = service.createSetupIntent(dto);
        return ResponseEntity.ok(clientSecret);
    }

}
