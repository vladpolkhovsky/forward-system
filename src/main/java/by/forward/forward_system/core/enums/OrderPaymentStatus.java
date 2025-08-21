package by.forward.forward_system.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderPaymentStatus {
    FULL_PAYMENT("Оплачен"),
    PARTITIONAL_PAYMENT("Частично оплачен"),
    REFUND("Возврат д. с."),
    NO_PAYMENT("Не оплачен"),
    ;

    private final String rusName;
}
