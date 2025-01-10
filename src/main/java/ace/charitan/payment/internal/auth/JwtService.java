package ace.charitan.payment.internal.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

@Setter
@Getter
@Service
class JwtService {
    private PublicKey encPublicKey;
    private PrivateKey sigPrivateKey;
    private PublicKey sigPublicKey;

    public Claims parseJwsPayload(String jws) {
        return Jwts.parser()
                   .verifyWith(sigPublicKey)
                   .build()
                   .parseSignedClaims(jws)
                   .getPayload();
    }

}
