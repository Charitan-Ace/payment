package ace.charitan.payment.internal.dto;

import lombok.Data;

@Data
public class ChargeRequestDto {
    public enum Currency {
        EUR, USD;
    }
    private String description;
    private int amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;
}
