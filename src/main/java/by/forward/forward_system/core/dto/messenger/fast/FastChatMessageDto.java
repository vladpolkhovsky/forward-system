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
    private long chatId;
    private String chatName;
    private long createdAtTimestamp;
    private Long fromUserId;
    private String fromUserUsername;
    private boolean isSystemMessage;
    private String createdAt;
    private String text;
    private String messageType;
    private boolean isViewed;
    private boolean isHidden;
    private List<FastChatMessageAttachmentDto> attachments;
    private List<FastChatMessageOptionDto> options;
}
