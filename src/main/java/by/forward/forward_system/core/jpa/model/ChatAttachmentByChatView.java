package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "attachments_by_message", schema = "forward_system")
public class ChatAttachmentByChatView {

    @Id
    @Column(name = "id")
    private Long attachmentId;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    private AttachmentEntity attachment;

    @Column(name = "from_user_id", insertable = false, updatable = false)
    private Long userId;

    @OneToOne
    @JoinColumn(name = "from_user_id")
    private UserEntity user;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}