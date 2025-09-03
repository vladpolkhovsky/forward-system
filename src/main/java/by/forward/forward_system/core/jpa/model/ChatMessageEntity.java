package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private Set<ChatMessageAttachmentEntity> chatMessageAttachmentsSet = new HashSet<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private Set<ChatMessageOptionEntity> chatMessageOptionsSet = new HashSet<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private Set<ChatMessageToUserEntity> chatMessageToUsersSet = new HashSet<>();

    @Deprecated
    public List<ChatMessageOptionEntity> getChatMessageOptions() {
        return List.copyOf(getChatMessageOptionsSet());
    }

    @Deprecated
    public List<ChatMessageToUserEntity> getChatMessageToUsers() {
        return List.copyOf(getChatMessageToUsersSet());
    }

    @Deprecated
    public List<ChatMessageAttachmentEntity> getChatMessageAttachments() {
        return List.copyOf(getChatMessageAttachmentsSet());
    }
}