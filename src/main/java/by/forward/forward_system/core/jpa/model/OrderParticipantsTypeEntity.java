package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.ParticipantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_participants_type", schema = "forward_system")
public class OrderParticipantsTypeEntity {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public ParticipantType getType() {
        return ParticipantType.byName(getName());
    }

}