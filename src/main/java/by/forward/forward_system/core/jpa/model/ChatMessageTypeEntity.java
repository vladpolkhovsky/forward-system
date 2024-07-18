package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.ChatMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_message_types", schema = "forward_system")
public class ChatMessageTypeEntity {
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public ChatMessageType getType() {
        return ChatMessageType.byName(getName());
    }

}