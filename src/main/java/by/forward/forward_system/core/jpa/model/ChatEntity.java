package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.ChatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "chats", schema = "forward_system")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "chat_name", nullable = false, length = 1024)
    private String chatName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(name = "last_message_date", nullable = false)
    private LocalDateTime lastMessageDate;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "chat", cascade = CascadeType.PERSIST)
    private ChatMetadataEntity chatMetadata;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type", nullable = false)
    private ChatTypeEntity chatType;

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<ChatMessageEntity> chatMassages = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = "forward_system", name = "chat_members",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> participants = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = "forward_system", name = "chat_to_tag",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<TagEntity> tags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<ChatMessageToUserEntity> chatMessageToUsers = new ArrayList<>();

    public static ChatEntity of(Long chatId) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setId(chatId);
        return chatEntity;
    }

    public boolean isChatTypeIs(ChatType type) {
        return getChatType().getType().equals(type);
    }
}