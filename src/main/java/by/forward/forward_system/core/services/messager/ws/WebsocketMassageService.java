package by.forward.forward_system.core.services.messager.ws;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.MessageToUserDto;
import by.forward.forward_system.core.dto.websocket.WSAttachment;
import by.forward.forward_system.core.dto.websocket.WSChatMessage;
import by.forward.forward_system.core.events.events.CheckMessageByAiEvent;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.messager.SpamDetectorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class WebsocketMassageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final MessageService messageService;

    private final SpamDetectorService spamDetectorService;

    private final BanService banService;

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskScheduler taskScheduler;

    @Transactional
    public void handleWebsocketMessage(WSChatMessage chatMessage) {
        Long chatId = chatMessage.getChatId();
        Long userId = chatMessage.getUserId();

        String message = chatMessage.getMessage();
        List<Long> attachmentIds = Optional.ofNullable(chatMessage.getAttachments()).orElse(Collections.emptyList()).stream()
            .map(WSAttachment::getFileAttachmentId)
            .toList();

        log.info(
            "Новое сообщение в чате {} от пользователя {}, контент = \"{}\", приложенные файлы = {}",
            chatId,
            userId,
            RegExUtils.replaceAll(StringUtils.abbreviate(message, 100), "\\R+", " "),
            attachmentIds
        );

        if (spamDetectorService.isSpam(chatId)) {
            if (banService.ban(userId, "Превышен лимит сообщений в чате.", true, List.of())) {
                notifyBanned(Collections.singletonList(userId), chatId);
                return;
            }
        }

        boolean isBanned = banService.isBanned(userId);

        if (isBanned) {
            notifyBanned(Collections.singletonList(userId), chatId);
            return;
        }

        MessageDto messageDto = null;

        for (int i = 0; i < 3; i++) {
            try {
                messageDto = messageService.handleWsMessage(chatId, userId, message, attachmentIds);
            } catch (Exception ex) {
                log.error("Error in handleWsMessage userId = {}. Attempt = {}", userId, i, ex);
                continue;
            }
            break;
        }

        if (messageDto == null) {
            log.error("Ошибка обработки сообщения от пользователя userId = {}", userId);
            throw new RuntimeException("Error in handleWsMessage userId = " + userId + ", message = " + message);
        }

        long messageId = messageDto.getId();
        taskScheduler.schedule(() -> applicationEventPublisher.publishEvent(new CheckMessageByAiEvent(messageId)), plusSeconds(5));

        List<Long> chatMemberIds = new ArrayList<>(messageDto.getMessageToUser().stream()
            .map(MessageToUserDto::getUserId)
            .toList());
        chatMemberIds.add(userId);

        sendMessageToUsers(chatMemberIds, messageDto);
    }

    public Instant plusSeconds(int seconds) {
        return LocalDateTime.now()
            .plusSeconds(seconds)
            .atZone(ZoneId.of("Europe/Moscow"))
            .toInstant();
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

    public void sendErrorMessage(Long userId, String message) {
        WSNotification<String> errorMessage = new WSNotification<>();
        errorMessage.setType(WSNotification.NotificationTypes.ERROR);
        errorMessage.setValue(message);

        simpMessagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages",
            errorMessage
        );
    }

    public void notifyError(Long userId, Long chatId, Throwable ex) {
        String userName = userRepository.findById(userId).map(UserEntity::getUsername).orElse("Не найден пользователь. userId = " + userId);
        String chatName = chatRepository.findChatNameById(chatId).orElse("Не найдено имя чата. chatId = " + chatId);

        String text = """
            Ошибика отправки сообщения в чате: %s\n
            Сообщение от пользователя: %s\n
            Стекстрейс ошибки: \n
            %s
            """.formatted(chatName, userName, ExceptionUtils.getStackTrace(ex));

        messageService.sendMessageToErrorChat(text);

        sendErrorMessage(userId, "Ошибка отправки сообщения в чат: \"%s\"".formatted(chatName));
    }
}