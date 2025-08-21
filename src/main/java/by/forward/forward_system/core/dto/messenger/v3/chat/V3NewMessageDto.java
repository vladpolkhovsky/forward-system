package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3NewMessageDto {
    private Long id;
    private Long chatId;
    private String chatName;
    private ChatType chatType;
    private Long fromUserId;
    private String fromUserUsername;
    private String message;
}
