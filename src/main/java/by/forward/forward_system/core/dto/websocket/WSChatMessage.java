package by.forward.forward_system.core.dto.websocket;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WSChatMessage {
    private Long chatId;
    private Long userId;
    private String message;
    private List<WSAttachment> attachments;
}
