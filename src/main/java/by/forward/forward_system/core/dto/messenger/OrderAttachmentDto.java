package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderAttachmentDto {
    private Long id;
    private Long orderId;
    private Long attachmentId;
    private String attachmentName;
}
