package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FastChatMessageDto {
    private long id;
    private Long fromUserId;
    private boolean isSystemMessage;
    private String createdAt;
    private String text;
    private boolean isViewed;
    private List<FastChatMessageAttachmentDto> attachments;
    private List<FastChatMessageOptionDto> options;
}
