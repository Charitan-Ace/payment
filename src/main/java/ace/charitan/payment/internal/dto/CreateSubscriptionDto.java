package ace.charitan.payment.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubscriptionDto {
    private String projectId;
    private Double amount;
    private String successUrl;
    private String cancelUrl;
}
