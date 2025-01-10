package ace.charitan.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentIntentDto {
    @NonNull
    private Long donationId;
    @NonNull
    private Long amount;
    @NonNull
    private String currency;
}
