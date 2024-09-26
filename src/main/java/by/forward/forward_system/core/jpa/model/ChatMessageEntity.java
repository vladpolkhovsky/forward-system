package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_messages", schema = "forward_system")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_type", nullable = false)
    private ChatMessageTypeEntity chatMessageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private UserEntity fromUser;

    @Column(name = "is_system_message", nullable = false)
    private Boolean isSystemMessage = false;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @Column(name = "content", length = 65536)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "message")
    private List<ChatMessageAttachmentEntity> chatMessageAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "message")
    private List<ChatMessageOptionEntity> chatMessageOptions = new ArrayList<>();

    @OneToMany(mappedBy = "message")
    private List<ChatMessageToUserEntity> chatMessageToUsers = new ArrayList<>();

}