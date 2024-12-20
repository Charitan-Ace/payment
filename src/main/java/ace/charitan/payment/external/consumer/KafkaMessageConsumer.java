package ace.charitan.payment.external.consumer;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageConsumer {

    @KafkaListener(topics = "payment-test", groupId = "payment")
    public void listen(String message) {
        System.out.println("Payment microservice received message: " + message);
    }

}

