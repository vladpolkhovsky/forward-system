package by.forward.forward_system.core.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED("CREATED", "Создан"),
    DISTRIBUTION("DISTRIBUTION", "На распределении"),
    ADMIN_REVIEW("ADMIN_REVIEW", "На проверке админа"),
    IN_PROGRESS("IN_PROGRESS", "В работе"),
    REVIEW("REVIEW", "Проверка"),
    GUARANTEE("GUARANTEE", "На гарантии"),
    FINALIZATION("FINALIZATION", "Доработка"),
    CLOSED("CLOSED", "Завершен"),

    ;

    private final String name;

    private final String rusName;

    OrderStatus(String name, String rusName) {
        this.name = name;
        this.rusName = rusName;
    }

    public static OrderStatus byName(String name) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name.equals(name)) {
                return status;
            }
        }
        return null;
    }

    public int getOrdinal() {
        OrderStatus[] values = OrderStatus.values();
        for (int i = 0; i < values.length; i++) {
            if (this == values[i]) {
                return i;
            }
        }
        return -1;
    }
}
