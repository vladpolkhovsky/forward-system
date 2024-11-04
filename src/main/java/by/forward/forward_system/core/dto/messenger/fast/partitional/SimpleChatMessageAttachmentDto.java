package by.forward.forward_system.core.dto.messenger.fast.partitional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleChatMessageAttachmentDto {
    private long id;
    private long messageId;
    private long attachmentId;
    private String filename;
}
