package ace.charitan.payment.internal.service;

import ace.charitan.common.dto.donation.CreateMonthlyDonationDto;
import ace.charitan.common.dto.donation.UpdateDonationStripeIdDto;
import ace.charitan.common.dto.email.payment.EmailPaymentHaltedProjectCancelSubscriptionEmailDto;
import ace.charitan.common.dto.profile.donor.DonorProfileDto;
import ace.charitan.common.dto.profile.donor.DonorsDto;
import ace.charitan.common.dto.profile.donor.GetDonorProfileByIdsRequestDto;
import ace.charitan.common.dto.profile.donor.GetDonorProfileByIdsResponseDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
class KafkaMessageProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;

    public void sendMessage(String topic, String message) {
        System.out.println("Payment microservice sent message: " + message);
        kafkaTemplate.send(topic, message);
    }

    public void updateDonationStripeId(Long donationId, String transactionStripeId) {
        UpdateDonationStripeIdDto dto = new UpdateDonationStripeIdDto(donationId, transactionStripeId);
        kafkaTemplate.send("update-donation-stripe-id", dto);
    }

    public DonorsDto getDonorProfilesById(List<UUID> ids) throws ExecutionException, InterruptedException {
        GetDonorProfileByIdsRequestDto dto = new GetDonorProfileByIdsRequestDto(ids);

        ProducerRecord<String, Object> record = new ProducerRecord<>("profile.get.donors.profile", dto);

        RequestReplyFuture<String, Object, Object> future = replyingKafkaTemplate.sendAndReceive(record);

        GetDonorProfileByIdsResponseDto response = (GetDonorProfileByIdsResponseDto) future.get().value();

        return response.donorProfileListDto();
    }

    public void createMonthlyDonation(Double amount, String message, String transactionStripeId, String projectId, String donorId) {
        CreateMonthlyDonationDto dto = new CreateMonthlyDonationDto(amount, message, transactionStripeId, projectId, donorId);
        kafkaTemplate.send("create-monthly-donation", dto);
    }

    public void sendCancelSubscriptionEmail(EmailPaymentHaltedProjectCancelSubscriptionEmailDto dto) {
        kafkaTemplate.send("email.subscription.cancel", dto);
    }

}