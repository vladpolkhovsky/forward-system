package by.forward.forward_system.core.dto.messenger.fast.partitional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleChatMessageDto {
    private long id;
    private long chatId;
    private String text;
    private boolean isSystem;
    private String messageType;
    private Long fromUserId;
    private String fromUserUsername;
    private String date;
    private long createdAtTimestamp;
    private boolean isViewed;
    private boolean isHidden;
}
