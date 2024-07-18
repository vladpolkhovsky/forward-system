package by.forward.forward_system.core.web.websocket;

import by.forward.forward_system.core.dto.websocket.WSChatMessage;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class ChatController {

    private final WebsocketMassageService websocketMassageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload WSChatMessage chatMessage) {
        websocketMassageService.handleWebsocketMessage(chatMessage);
    }

}
