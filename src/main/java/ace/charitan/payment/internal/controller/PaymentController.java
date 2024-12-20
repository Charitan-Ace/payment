package ace.charitan.payment.internal.controller;

import ace.charitan.payment.internal.dto.ConfirmPaymentIntentDto;
import ace.charitan.payment.internal.dto.CreatePaymentIntentDto;
import ace.charitan.payment.internal.dto.CreateCustomerDto;
import ace.charitan.payment.internal.service.InternalPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
class PaymentController {

    @Autowired
    private InternalPaymentService service;

    @PostMapping("/intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody CreatePaymentIntentDto dto) throws StripeException {
        PaymentIntent intent = service.createPaymentIntent(dto);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = intent.toJson();
            Map<String, Object> responseMap = mapper.readValue(jsonString, Map.class);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process customer data"));
        }
    }

    @PostMapping("/customer")
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody CreateCustomerDto dto) throws StripeException {
        Customer customer = service.createCustomer(dto);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = customer.toJson();
            Map<String, Object> responseMap = mapper.readValue(jsonString, Map.class);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process customer data"));
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<Map<String, Object>> getCustomer(@RequestParam String id) throws StripeException {
        Customer customer = service.getCustomer(id);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = customer.toJson();
            Map<String, Object> responseMap = mapper.readValue(jsonString, Map.class);
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process customer data"));
        }
    }

    @PutMapping("/intent")
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

}
