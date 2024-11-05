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
    private String text;
    private boolean isSystem;
    private Long fromUserId;
    private String date;
    private boolean isViewed;
    private boolean isHidden;
}
