package ace.charitan.payment.internal.auth;

import io.jsonwebtoken.security.Jwks;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

@Component
class JwtConsumer implements ConsumerSeekAware {

    private static final Logger logger = LoggerFactory.getLogger(JwtConsumer.class);

    @Autowired
    private JwtService jwtService;


    @KafkaListener(topics = "key.encryption.public.change")
    public void encPublicKeyConsumer(String jwkString) {
        Key jwk = Jwks.parser()
                .build()
                .parse(jwkString)
                .toKey();
        if (jwk instanceof PublicKey) {
            jwtService.setEncPublicKey((PublicKey) jwk);
            logger.info("Encryption {} public key updated", jwk.getFormat());
        }
    }

    @KafkaListener(topics = "key.signature.public.change")
    public void sigPublicKeyConsumer(String jwkString) {
        Key jwk = Jwks.parser()
                .build()
                .parse(jwkString)
                .toKey();
        if (jwk instanceof PublicKey) {
            jwtService.setSigPublicKey((PublicKey) jwk);
            logger.info("Signature {} public key updated", jwk.getFormat());
        }
    }

    @KafkaListener(topics = "key.signature.private.change")
    public void sigPrivateKeyConsumer(String jwkString) {
        Key jwk = Jwks.parser()
                .build()
                .parse(jwkString)
                .toKey();
        if (jwk instanceof PrivateKey) {
            jwtService.setSigPrivateKey((PrivateKey) jwk);
            logger.info("Signature {} private key updated", jwk.getFormat());
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.keySet().forEach(topicPartition -> {
            callback.seekRelative(topicPartition.topic(), topicPartition.partition(), -1, false);
        });
    }
}
