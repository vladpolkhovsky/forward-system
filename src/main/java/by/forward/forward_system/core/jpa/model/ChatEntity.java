package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "chat")
    private ChatMetadataEntity chatMetadata;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type", nullable = false)
    private ChatTypeEntity chatType;

    @OneToMany(mappedBy = "chat")
    private List<ChatMessageEntity> chatMassages = new ArrayList<>();

    @OneToMany(mappedBy = "chat")
    private List<ChatMemberEntity> chatMembers = new ArrayList<>();

    @OneToMany(mappedBy = "chat")
    private List<ChatMessageToUserEntity> chatMessageToUsers = new ArrayList<>();

}