package ace.charitan.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentIntentDto {
    private String userId;
    private Long donationId;
    private Double amount;
    private String currency;
    private String successUrl;
    private String cancelUrl;
}
