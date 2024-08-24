package by.forward.forward_system.core.services.messager.ws;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.MessageToUserDto;
import by.forward.forward_system.core.dto.websocket.WSChatMessage;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.messager.SpamDetectorService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class WebsocketMassageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final MessageService messageService;

    private final AttachmentService attachmentService;

    private final SpamDetectorService spamDetectorService;

    private final BanService banService;

    public void handleWebsocketMessage(WSChatMessage chatMessage) {
        Long chatId = chatMessage.getChatId();
        Long userId = chatMessage.getUserId();

        if (spamDetectorService.isSpam(chatId)) {
            if (banService.ban(userId, "Превышен лимит сообщений в чате.")) {
                notifyBanned(Collections.singletonList(userId), chatId);
                return;
            }
        }

        String message = chatMessage.getMessage();
        List<Long> attachmentIds = Collections.emptyList();

        if (!CollectionUtils.isEmpty(chatMessage.getAttachments())) {
            attachmentIds = attachmentService.getAttachment(chatMessage.getAttachments());
        }

        MessageDto messageDto = messageService.handleWsMessage(chatId, userId, message, attachmentIds);

        List<Long> chatMemberIds = new ArrayList<>(messageDto.getMessageToUser().stream()
            .map(MessageToUserDto::getUserId)
            .toList());
        chatMemberIds.add(userId);

        sendMessageToUsers(chatMemberIds, messageDto);
    }

    public void sendMessageToUsers(List<Long> userIds, MessageDto chatMessage) {
        WSNotification<MessageDto> notification = new WSNotification<>();
        notification.setType(WSNotification.NotificationTypes.MESSAGE);
        notification.setValue(chatMessage);
        send(userIds, notification);
    }

    public void notifyNewChatCreated(List<Long> userIds, Long chatId) {
        WSNotification<Long> notification = new WSNotification<>();
        notification.setType(WSNotification.NotificationTypes.NEW_CHAT);
        notification.setValue(chatId);
        send(userIds, notification);
    }

    public void notifyBanned(List<Long> userIds, Long chatId) {
        WSNotification<Long> notification = new WSNotification<>();
        notification.setType(WSNotification.NotificationTypes.BANNED);
        notification.setValue(chatId);
        send(userIds, notification);
    }

    private void send(List<Long> userIds, WSNotification<?> notification) {
        List<Long> users = userIds.stream().distinct().toList();
        for (Long userId : users) {
            simpMessagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                notification
            );
        }
    }
}