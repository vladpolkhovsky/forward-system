package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageToUserDto {
    private Long id;
    private Long chatId;
    private Long messageId;
    private Long userId;
    private Boolean isViewed;
    private LocalDateTime createdAt;
}
