package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_message_options", schema = "forward_system")
public class ChatMessageOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessageEntity message;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_participant", nullable = false)
    private OrderParticipantsTypeEntity orderParticipant;

    @Column(name = "option_resolved", nullable = false)
    private Boolean optionResolved = false;

    @Column(name = "content", length = 2048)
    private String content;

    @Column(name = "option_name", length = 2048)
    private String optionName;
}