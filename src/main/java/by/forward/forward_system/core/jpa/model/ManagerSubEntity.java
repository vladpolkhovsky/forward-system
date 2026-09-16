package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "manager_sub", schema = "forward_system")
public class ManagerSubEntity {

    @Id
    @Column(name = "manager_id", nullable = false)
    private Long mangerId;

    @Column(name = "sub_manager_id")
    private Long subManagerId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
