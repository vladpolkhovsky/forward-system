package by.forward.forward_system.core.enums;

import lombok.Getter;

@Getter
public enum ChatType {
    REQUEST_ORDER_CHAT("REQUEST_ORDER_CHAT", "Чат обсуждение заказ"),
    ORDER_CHAT("ORDER_CHAT", "Чат заказа"),
    OTHER_CHAT("OTHER_CHAT", "Простой чат"),
    ;

    private final String name;

    private final String rusName;

    ChatType(String name, String rusName) {
        this.name = name;
        this.rusName = rusName;
    }

    public static ChatType byName(String name) {
        for (ChatType status : ChatType.values()) {
            if (status.name.equals(name)) {
                return status;
            }
        }
        return null;
    }
}
