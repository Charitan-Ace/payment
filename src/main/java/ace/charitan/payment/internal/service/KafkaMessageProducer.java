package ace.charitan.payment.internal.service;

import ace.charitan.common.dto.donation.UpdateDonationStripeIdDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaMessageProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        System.out.println("Payment microservice sent message: " + message);
        kafkaTemplate.send(topic, message);
    }

    public void updateDonationStripeId(Long donationId, String transactionStripeId) {
        UpdateDonationStripeIdDto dto = new UpdateDonationStripeIdDto(donationId, transactionStripeId);
        kafkaTemplate.send("update-donation-stripe-id", dto);
    }

}