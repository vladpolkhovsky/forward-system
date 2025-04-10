package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_message_attachments", schema = "forward_system")
public class ChatMessageAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessageEntity message;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private AttachmentEntity attachment;

    public static ChatMessageAttachmentEntity of(AttachmentEntity attachment) {
        ChatMessageAttachmentEntity result = new ChatMessageAttachmentEntity();
        result.setAttachment(attachment);
        return result;
    }
}