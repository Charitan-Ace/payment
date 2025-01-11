package ace.charitan.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSetupIntentDto {
    private String successUrl;
    private String cancelUrl;
}
