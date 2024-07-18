package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageAttachmentDto {
    private Long id;
    private Long messageId;
    private Long attachmentId;
    private String attachmentName;
}
