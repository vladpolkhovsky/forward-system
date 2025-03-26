package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bot_integration_data", schema = "forward_system")
public class BotIntegrationDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bot_type", nullable = false)
    private BotTypeEntity botType;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "telegram_user_id")
    private Long telegramUserId;

}