package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customer_telegram_to_forward_order", schema = "forward_system")
public class CustomerTelegramToForwardOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forward_order_id", nullable = false)
    private ForwardOrderEntity forwardOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bot_integration_data_id", nullable = false)
    private BotIntegrationDataEntity botIntegrationData;

    @ColumnDefault("now()")
    @Column(name = "last_update_at", nullable = false)
    private LocalDateTime lastUpdateAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}