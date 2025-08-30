package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.DistributionItemStatusType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "queue_distribution_item", schema = "forward_system")
public class QueueDistributionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "distribution_order", nullable = false)
    private Long distributionOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queue_distribution_id", nullable = false)
    private QueueDistributionEntity queueDistribution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "status", nullable = false, length = 75)
    @Enumerated(EnumType.STRING)
    private DistributionItemStatusType status;

    @Column(name = "wait_start")
    private LocalDateTime waitStart;

    @Column(name = "wait_until")
    private LocalDateTime waitUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}