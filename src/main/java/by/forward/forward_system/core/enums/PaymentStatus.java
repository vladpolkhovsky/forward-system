package by.forward.forward_system.core.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    WAITING_SIGNED_FILE("WAITING_SIGNED_FILE", "Ожидание подписанного файла"),
    WAITING_PAYMENT("WAITING_PAYMENT", "Ожидание выплаты"),
    WAITING_CHECK("WAITING_CHECK", "Ожидание чека"),
    VERIFICATION("VERIFICATION", "Проверка"),
    CLOSED("CLOSED", "Заверешено"),
    ANNULLED("ANNULLED", "Аннулирован"),

    ;

    PaymentStatus(String name, String rusName) {
        this.name = name;
        this.rusName = rusName;
    }

    private final String name;

    private final String rusName;

    public static PaymentStatus byName(String name) {
        for (PaymentStatus type : PaymentStatus.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

}
