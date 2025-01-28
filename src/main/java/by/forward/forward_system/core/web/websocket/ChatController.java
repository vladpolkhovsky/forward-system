package by.forward.forward_system.core.web.websocket;

import by.forward.forward_system.core.dto.websocket.WSChatMessage;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Collections;

@Controller
@AllArgsConstructor
public class ChatController {

    private final WebsocketMassageService websocketMassageService;

    private final BanService banService;

    @MessageMapping("/chat")
    public void processMessage(@Payload WSChatMessage chatMessage) {
        if (banService.isBanned(chatMessage.getUserId())) {
            websocketMassageService.notifyBanned(
                Collections.singletonList(chatMessage.getUserId()),
                chatMessage.getChatId()
            );
            return;
        }
        try {
            websocketMassageService.handleWebsocketMessage(chatMessage);
        } catch (Exception e) {
            websocketMassageService.notifyError(chatMessage.getUserId(), chatMessage.getChatId());
        }
    }

}
