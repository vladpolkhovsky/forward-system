package by.forward.forward_system.jobs;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.ChatMetadataRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.NotificationOutboxRepository;
import by.forward.forward_system.core.jpa.repository.SkipChatNotificationRepository;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class BotNotificationJob {

    private final BotNotificationService botNotificationService;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ChatMetadataRepository chatMetadataRepository;
    private final ChatRepository chatRepository;
    private final SkipChatNotificationRepository skipChatNotificationRepository;

    //@Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void notifyBot() {
        LocalDateTime startTime = LocalDateTime.now();

        LocalDateTime time = LocalDateTime.now().minusMinutes(2);
        List<NotificationOutboxEntity> allMessagesOlderThen = notificationOutboxRepository.getAllMessagesOlderThen(time);

        Map<UserEntity, List<NotificationOutboxEntity>> userToOutbox = new HashMap<>();
        for (NotificationOutboxEntity notificationOutboxEntity : allMessagesOlderThen) {
            userToOutbox.putIfAbsent(notificationOutboxEntity.getUser(), new ArrayList<>());
            userToOutbox.get(notificationOutboxEntity.getUser()).add(notificationOutboxEntity);
        }

        int sandedMessageCount = 0;
        for (Map.Entry<UserEntity, List<NotificationOutboxEntity>> userEntityListEntry : userToOutbox.entrySet()) {
            try {
                sandedMessageCount += computeAndSendNotification(userEntityListEntry.getKey(), userEntityListEntry.getValue());
            } catch (Exception ignored) {

            }
        }

        notificationOutboxRepository.deleteAll(allMessagesOlderThen);

        long execTime = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        log.info("Отправка оповещений завершена. Заняло {} сек. Отправлено {} сообщений.", execTime, sandedMessageCount);
    }

    private int computeAndSendNotification(UserEntity user, List<NotificationOutboxEntity> notification) {
        Map<Long, Integer> chatIdToMessageCount = new HashMap<>();
        List<Long> skippedChatIdsByUserId = skipChatNotificationRepository.findSkippedChatIdsByUserId(user.getId());

        int sandedMessageCount = 0;
        for (NotificationOutboxEntity notificationOutboxEntity : notification) {
            Boolean isViewed = notificationOutboxEntity.getMessageToUser().getIsViewed();
            if (!isViewed) {
                chatIdToMessageCount.putIfAbsent(notificationOutboxEntity.getChat().getId(), 0);
                chatIdToMessageCount.put(
                    notificationOutboxEntity.getChat().getId(),
                    chatIdToMessageCount.get(notificationOutboxEntity.getChat().getId()) + 1
                );
            }
        }

        for (Map.Entry<Long, Integer> chatIdToMessageCountEntry : chatIdToMessageCount.entrySet()) {
            if (chatIdToMessageCountEntry.getValue() == 0) {
                continue;
            }
            if (isNoNeedToSendNotification(skippedChatIdsByUserId, user, chatIdToMessageCountEntry.getKey())) {
                continue;
            }
            ChatEntity chatEntity = chatRepository.findById(chatIdToMessageCountEntry.getKey()).get();
            String chatName = chatEntity.getChatName();
            ChatType chatType = chatEntity.getChatType().getType();
            sandedMessageCount += sendIfNeeded(user, chatType, chatName, chatEntity.getId(), chatIdToMessageCountEntry.getValue());
        }
        return sandedMessageCount;
    }

    private int sendIfNeeded(UserEntity user, ChatType chatType, String chatName, Long chatId, Integer newMessageCount) {
        int sandedMessageCount = 0;
        if (chatType.equals(ChatType.REQUEST_ORDER_CHAT)) {
            ChatMetadataEntity chatMetadataEntity = chatMetadataRepository.findById(chatId).get();
            if (chatMetadataEntity.getUser().getId().equals(user.getId()) || chatMetadataEntity.getManager().getId().equals(user.getId())) {
                sendRequestOrderNotification(user, chatName, newMessageCount);
                sandedMessageCount++;
            }
        } else if (chatType.equals(ChatType.ADMIN_TALK_CHAT)) {
            sendAdminTalkNotification(user, chatName, newMessageCount);
            sandedMessageCount++;
        } else if (chatType.equals(ChatType.ORDER_CHAT)) {
            ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
            String techNumber = "Не определён";
            boolean isNeedToSend = false;
            if (chatEntity.getOrder() != null) {
                OrderEntity orderEntity = chatEntity.getOrder();
                techNumber = orderEntity.getTechNumber();
                Optional<OrderParticipantEntity> any = orderEntity.getOrderParticipants().stream()
                    .filter(t -> t.getUser().getId().equals(user.getId()))
                    .findAny();
                isNeedToSend = any.isPresent();;
            }
            if (isNeedToSend) {
                sendOrderNotification(user, techNumber, chatName, newMessageCount);
                sandedMessageCount++;
            }
        } else if (chatType.equals(ChatType.SPECIAL_CHAT)) {
            sendSpecialAndOtherNotification(user, chatName, newMessageCount);
            sandedMessageCount++;
        } else if (chatType.equals(ChatType.OTHER_CHAT)) {
            sendSpecialAndOtherNotification(user, chatName, newMessageCount);
            sandedMessageCount++;
        }
        return sandedMessageCount;
    }

    private void sendAdminTalkNotification(UserEntity userEntity, String chatName, Integer newMessageCount) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            """.formatted(userEntity.getUsername(), newMessageCount, chatName);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendRequestOrderNotification(UserEntity userEntity, String chatName, Integer newMessageCount) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            """.formatted(userEntity.getUsername(), newMessageCount, chatName);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendOrderNotification(UserEntity userEntity, String techNumber, String chatName, Integer newMessageCount) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s" по заказу #%s.
            """.formatted(userEntity.getUsername(), newMessageCount, chatName, techNumber);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendSpecialAndOtherNotification(UserEntity userEntity, String chatName, Integer newMessageCount) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            """.formatted(userEntity.getUsername(), newMessageCount, chatName);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private boolean isNoNeedToSendNotification(List<Long> skippedChatIdsByUserId, UserEntity user, Long chatId) {
        if (skippedChatIdsByUserId.contains(chatId)) {
            return true;
        }
        ChatEntity chatEntity = chatRepository.findById(chatId).get();
        if (chatEntity.getChatType().getType().equals(ChatType.ADMIN_TALK_CHAT) && user.getAuthorities().contains(Authority.OWNER)) {
            return true;
        }
        return false;
    }
}
