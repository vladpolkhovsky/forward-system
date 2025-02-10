package by.forward.forward_system.core.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WSChatMessage {
    private Long chatId;
    private Long userId;
    private String message;
    private List<WSAttachment> attachments;
}
