package ace.charitan.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentIntentDto {
    private Long amount;
    private String currency;
    private String stripeCustomerId;
}
