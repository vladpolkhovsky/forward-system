package by.forward.forward_system.core.enums;

import lombok.Getter;

@Getter
public enum BotType {
    TELEGRAM_BOT("TELEGRAM_BOT", "Телеграм бот"),
    CUSTOMER_TELEGRAM_BOT("CUSTOMER_TELEGRAM_BOT", "Телеграм бот для заказчиков"),

    ;

    private final String name;

    private final String rusName;

    BotType(String name, String rusName) {
        this.rusName = rusName;
        this.name = name;
    }

    public static BotType byName(String name) {
        for (BotType type : BotType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
