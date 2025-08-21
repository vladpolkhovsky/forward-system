package by.forward.forward_system.core.enums;

import by.forward.forward_system.core.jpa.model.ChatMessageTypeEntity;
import lombok.Getter;

@Getter
public enum ChatMessageType {
    NEW_CHAT("NEW_CHAT", "NEW_CHAT"),
    NEW_ORDER("NEW_ORDER", "NEW_ORDER"),
    MESSAGE("MESSAGE", "MESSAGE"),
    MESSAGE_FROM_CUSTOMER("MESSAGE_FROM_CUSTOMER", "MESSAGE_FROM_CUSTOMER"),
    SYSTEM_MESSAGE("SYSTEM_MESSAGE", "SYSTEM_MESSAGE"),
    ;

    private final String name;

    private final String rusName;

    ChatMessageType(String name, String rusName) {
        this.rusName = rusName;
        this.name = name;
    }

    public static ChatMessageType byName(String name) {
        for (ChatMessageType type : ChatMessageType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public ChatMessageTypeEntity entity() {
        return new ChatMessageTypeEntity(getName());
    }
}
