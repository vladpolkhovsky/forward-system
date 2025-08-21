package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.ChatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_type", schema = "forward_system")
@AllArgsConstructor
@NoArgsConstructor
public class ChatTypeEntity {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public ChatType getType() {
        return ChatType.byName(getName());
    }
}
