package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.DistributionStatusType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "queue_distribution", schema = "forward_system")
public class QueueDistributionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "status", nullable = false, length = 75)
    @Enumerated(EnumType.STRING)
    private DistributionStatusType status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(name = "initial_message", length = 65536)
    private String createdByUserMessage;

    @Column(name = "wait_minutes", length = 65536)
    private Long queueDistributionWaitMinutes;

    @OneToMany(mappedBy = "queueDistribution", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("distributionOrder")
    private List<QueueDistributionItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}