package by.forward.forward_system.core.services.messager.ws;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.MessageToUserDto;
import by.forward.forward_system.core.dto.websocket.WSChatMessage;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.AIDetector;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.messager.SpamDetectorService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WebsocketMassageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final MessageService messageService;

    private final AttachmentService attachmentService;

    private final SpamDetectorService spamDetectorService;

    private final BanService banService;

    private final AIDetector aiDetector;

    private final UserRepository userRepository;

    public void handleWebsocketMessage(WSChatMessage chatMessage) {
        Long chatId = chatMessage.getChatId();
        Long userId = chatMessage.getUserId();

        if (spamDetectorService.isSpam(chatId)) {
            if (banService.ban(userId, "Превышен лимит сообщений в чате.", true, Collections.emptyList())) {
                notifyBanned(Collections.singletonList(userId), chatId);
                return;
            }
        }

        String username = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")).getUsername();
        String message = chatMessage.getMessage();
        List<Long> attachmentIds = Collections.emptyList();

        if (!CollectionUtils.isEmpty(chatMessage.getAttachments())) {
            attachmentIds = attachmentService.getAttachment(chatMessage.getAttachments());
        }

        boolean aiApproved = true;
        List<Long> logIds = new ArrayList<>();

        if (StringUtils.isNoneBlank(message)) {
            AIDetector.AICheckResult checkResult = aiDetector.isValidMessage(message, username);
            if (!checkResult.isOk()) {
                logIds.add(checkResult.aiLogId());
            }
            aiApproved &= checkResult.isOk();
        }

        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            for (Long attachmentId : attachmentIds) {
                AIDetector.AICheckResult checkResult = aiDetector.isValidFile(username, attachmentId);
                if (!checkResult.isOk()) {
                    logIds.add(checkResult.aiLogId());
                }
                aiApproved &= checkResult.isOk();
            }
        }

        boolean isBanned = false;
        if (!aiApproved) {
            isBanned = banService.ban(userId, formatBanReasonString(message, attachmentIds, logIds), logIds);
        }

        if (isBanned) {
            notifyBanned(Collections.singletonList(userId), chatId);
            return;
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

    private String formatBanReasonString(String message, List<Long> attachmentIds, List<Long> logIds) {
        String files = attachmentIds.stream().map(t -> "<a href=\"/load-file/%d\" target=\"_blank\">Файл</a>".formatted(t)).collect(Collectors.joining(" "));
        String aiLog = logIds.stream().map(t -> "<a href=\"/ai-log/%d\" target=\"_blank\">Лог проверки</a>".formatted(t)).collect(Collectors.joining(" "));
        return """
        Сообщение пользователя: "%s"
        Приложенные файлы: %s
        Лог проверки: %s
        Содержат данные, которые не прошли провреку.
        """.formatted(message, files, aiLog);
    }
}