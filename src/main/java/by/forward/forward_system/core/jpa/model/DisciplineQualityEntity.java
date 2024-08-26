package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.DisciplineQualityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "discipline_quality", schema = "forward_system")
public class DisciplineQualityEntity {
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public DisciplineQualityType getType() {
        return DisciplineQualityType.byName(getName());
    }
}