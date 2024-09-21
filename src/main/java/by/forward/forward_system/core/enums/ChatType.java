package by.forward.forward_system.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ChatType {
    REQUEST_ORDER_CHAT("REQUEST_ORDER_CHAT", "Чат обсуждение заказ"),
    ORDER_CHAT("ORDER_CHAT", "Чат заказа"),
    ADMIN_TALK_CHAT("ADMIN_TALK_CHAT", "Чат с администратором"),
    OTHER_CHAT("OTHER_CHAT", "Простой чат"),
    SPECIAL_CHAT("SPECIAL_CHAT", "Специальный чат"),
    ;

    public static final List<ChatType> noModerationChatTypes = Arrays.asList(OTHER_CHAT, SPECIAL_CHAT);

    public static final List<ChatType> noAiModerationChatTypes = Arrays.asList(OTHER_CHAT, SPECIAL_CHAT, ADMIN_TALK_CHAT);

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
