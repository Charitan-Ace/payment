package ace.charitan.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
class KafkaProducerConfig {
    @Bean
    public ConcurrentMessageListenerContainer<String, Object> replyContainer(
            ConsumerFactory<String, Object> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        ConcurrentMessageListenerContainer<String, Object> container = factory.createContainer("payment.reply");
        container.setAutoStartup(true);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentMessageListenerContainer<String, Object> container
    ) {
        return new ReplyingKafkaTemplate<>(producerFactory, container);
    }
}
