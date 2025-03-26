package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "update_order_request", schema = "forward_system")
public class UpdateOrderRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(name = "catcher_ids", length = 1024)
    private String catcherIds;

    @Column(name = "authors_ids", length = 1024)
    private String authorsIds;

    @Column(name = "hosts_ids", length = 1024)
    private String hostsIds;

    @Column(name = "experts_ids", length = 1024)
    private String expertsIds;

    @Column(name = "is_forward_order")
    private Boolean isForwardOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "new_status", nullable = false)
    private OrderStatusEntity newStatus;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "update_request_from_user_id", nullable = false)
    private UserEntity user;

    @Column(name = "is_viewed")
    private Boolean isViewed;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}