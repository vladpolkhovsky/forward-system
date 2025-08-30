package by.forward.forward_system.core.enums;

import by.forward.forward_system.core.jpa.model.OrderParticipantsTypeEntity;
import lombok.Getter;

@Getter
public enum ParticipantType {
    OWNER("OWNER", "Владелец"),
    CATCHER("CATCHER", "Менеджер Кетчер"),
    HOST("HOST", "Менеджер Хост"),
    AUTHOR("AUTHOR", "Автор на рассмотрении"),
    DECLINE_AUTHOR("DECLINE_AUTHOR", "Отказавшийся автор"),
    MAIN_AUTHOR("MAIN_AUTHOR", "Взявший в работу автор"),
    EXPERT("EXPERT", "Эксперт");

    private final String name;

    private final String rusName;

    ParticipantType(String name, String rusName) {
        this.name = name;
        this.rusName = rusName;
    }

    public static ParticipantType byName(String name) {
        for (ParticipantType type : ParticipantType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public OrderParticipantsTypeEntity toEntity() {
        return new OrderParticipantsTypeEntity(name);
    }
}
