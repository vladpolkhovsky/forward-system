package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_plan", schema = "forward_system")
@AllArgsConstructor
@NoArgsConstructor
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "target_sum", nullable = false)
    private Long targetSum;

    @Column(name = "plan_start", nullable = false)
    private LocalDateTime start;

    @Column(name = "plan_end", nullable = false)
    private LocalDateTime end;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
