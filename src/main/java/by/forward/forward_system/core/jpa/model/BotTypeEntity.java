package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.BotType;
import by.forward.forward_system.core.enums.ChatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bot_type", schema = "forward_system")
public class BotTypeEntity {
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public BotType getType() {
        return BotType.byName(getName());
    }
}