package by.forward.forward_system.jobs;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.ChatMetadataRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.NotificationOutboxRepository;
import by.forward.forward_system.core.jpa.repository.SkipChatNotificationRepository;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotNotificationJob {

    private static final int WAIT_MINS = 10;

    private final Semaphore getSemaphoreSemaphore = new Semaphore(1);
    private final Map<Long, Semaphore> semaphorePerUser = Collections.synchronizedMap(new HashMap<>());

    private final BotNotificationService botNotificationService;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ChatMetadataRepository chatMetadataRepository;
    private final ChatRepository chatRepository;
    private final SkipChatNotificationRepository skipChatNotificationRepository;

    private BotNotificationJob botNotificationJob;

    @Autowired
    public void setBotNotificationJob(BotNotificationJob notificationJob) {
        this.botNotificationJob = notificationJob;
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyByChatId(Long chatId) {

        getSemaphoreSemaphore.acquire();

        Semaphore semaphore = semaphorePerUser.getOrDefault(chatId, new Semaphore(1));
        semaphorePerUser.put(chatId, semaphore);

        getSemaphoreSemaphore.release();

        try {
            semaphore.acquire();
            log.info("Начало отправки уведомлений для чата {}", chatId);
            List<NotificationOutboxEntity> allMessagesOlderThen = notificationOutboxRepository.getAllMessagesByChatId(chatId);
            botNotificationJob.process(allMessagesOlderThen);
        } catch (Exception ex) {
            log.error("Error in notifyByChatId chatId={}", chatId, ex);
        } finally {
            log.info("Завершение отправки уведомлений для чата {}", chatId);
            semaphore.release();
        }
    }

    @Scheduled(fixedDelay = WAIT_MINS, timeUnit = TimeUnit.MINUTES)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyBot() {
        LocalDateTime time = LocalDateTime.now().minusMinutes(WAIT_MINS);
        List<NotificationOutboxEntity> allMessagesOlderThen = notificationOutboxRepository.getAllMessagesOlderThen(time);
        botNotificationJob.process(allMessagesOlderThen);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(List<NotificationOutboxEntity> allMessagesOlderThen) {
        LocalDateTime startTime = LocalDateTime.now();

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
        Map<Long, List<String>> chatIdToMessage = new HashMap<>();
        List<Long> skippedChatIdsByUserId = skipChatNotificationRepository.findSkippedChatIdsByUserId(user.getId());

        int sandedMessageCount = 0;
        for (NotificationOutboxEntity notificationOutboxEntity : notification) {
            Boolean isViewed = notificationOutboxEntity.getMessageToUser().getIsViewed();

            if (!isViewed) {
                chatIdToMessageCount.putIfAbsent(notificationOutboxEntity.getChat().getId(), 0);
                chatIdToMessage.putIfAbsent(notificationOutboxEntity.getChat().getId(), new ArrayList<>());

                ChatMessageEntity message = notificationOutboxEntity.getMessage();
                String username = Optional.ofNullable(message.getFromUser()).map(UserEntity::getUsername).orElse("Система");
                String content = StringUtils.abbreviate(
                    Optional.ofNullable(message.getContent()).filter(Predicate.not(StringUtils::isBlank)).orElse("Сообщение без текста."),
                    200
                );

                chatIdToMessageCount.put(
                    notificationOutboxEntity.getChat().getId(),
                    chatIdToMessageCount.get(notificationOutboxEntity.getChat().getId()) + 1
                );

                chatIdToMessage.get(notificationOutboxEntity.getChat().getId())
                    .add(String.format("«%s : %s»", username, content));
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

            sandedMessageCount += sendIfNeeded(user,
                chatType,
                chatName,
                chatEntity.getId(),
                chatIdToMessageCountEntry.getValue(),
                String.join("\n", chatIdToMessage.get(chatIdToMessageCountEntry.getKey()))
            );
        }

        return sandedMessageCount;
    }

    private int sendIfNeeded(UserEntity user, ChatType chatType, String chatName, Long chatId, Integer newMessageCount, String messageText) {
        int sandedMessageCount = 0;

        if (chatType.equals(ChatType.REQUEST_ORDER_CHAT)) {
            ChatMetadataEntity chatMetadataEntity = chatMetadataRepository.findById(chatId).get();
            if (chatMetadataEntity.getUser().getId().equals(user.getId()) || chatMetadataEntity.getManager().getId().equals(user.getId())) {
                sendRequestOrderNotification(user, chatName, newMessageCount, messageText);
                sandedMessageCount++;
            }
        } else if (chatType.equals(ChatType.ADMIN_TALK_CHAT)) {
            sendAdminTalkNotification(user, chatName, newMessageCount, messageText);
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
                isNeedToSend = any.isPresent();
            }

            if (isNeedToSend) {
                sendOrderNotification(user, techNumber, chatName, newMessageCount, messageText);
                sandedMessageCount++;
            }
        } else if (chatType.equals(ChatType.SPECIAL_CHAT)) {
            sendSpecialAndOtherNotification(user, chatName, newMessageCount, messageText);
            sandedMessageCount++;
        } else if (chatType.equals(ChatType.OTHER_CHAT)) {
            sendSpecialAndOtherNotification(user, chatName, newMessageCount, messageText);
            sandedMessageCount++;
        }
        return sandedMessageCount;
    }

    private void sendAdminTalkNotification(UserEntity userEntity, String chatName, Integer newMessageCount, String messageText) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            ---
            %s
            """.formatted(userEntity.getUsername(), newMessageCount, chatName, messageText);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendRequestOrderNotification(UserEntity userEntity, String chatName, Integer newMessageCount, String messageText) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            ---
            %s
            """.formatted(userEntity.getUsername(), newMessageCount, chatName, messageText);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendOrderNotification(UserEntity userEntity, String techNumber, String chatName, Integer newMessageCount, String messageText) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s" по заказу #%s.
            ---
            %s
            """.formatted(userEntity.getUsername(), newMessageCount, chatName, techNumber, messageText);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private void sendSpecialAndOtherNotification(UserEntity userEntity, String chatName, Integer newMessageCount, String messageText) {
        String text = """
            Здравствуйте, %s.
            У вас новые непрочитанное сообщения (%d шт.) в чате:
            "%s".
            ---
            %s
            """.formatted(userEntity.getUsername(), newMessageCount, chatName, messageText);
        botNotificationService.sendBotNotification(userEntity.getId(), text);
    }

    private boolean isNoNeedToSendNotification(List<Long> skippedChatIdsByUserId, UserEntity user, Long chatId) {
        if (skippedChatIdsByUserId.contains(chatId)) {
            return true;
        }
        if (user.hasAuthority(Authority.BANNED) || user.getDeleted()) {
            return true;
        }
        ChatEntity chatEntity = chatRepository.findById(chatId).get();
        return chatEntity.isChatTypeIs(ChatType.ADMIN_TALK_CHAT)
               && user.hasAuthority(Authority.OWNER);
    }
}
