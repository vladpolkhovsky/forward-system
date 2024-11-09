package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.SkipChatNotificationEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.SkipChatNotificationRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class StopNotificationFromChatResolver implements CommandResolver {

    private final BotNotificationService botNotificationService;

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final SkipChatNotificationRepository skipChatNotificationRepository;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/stop-notification");
    }

    @Override
    @Transactional
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        Optional<Long> userId = botNotificationService.getUserIdByChatId(update.getMessage().getChatId());

        if (userId.isEmpty()) {
            telegramClient.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Вы не зарегистрированы. Используйте: /reg <код>")
                .build());
            return;
        }

        String[] s = update.getMessage().getText().split(" ");

        if (s.length != 2) {
            telegramClient.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Неправильный набор аргументов: Используйте: /stop-notification <chat-id>")
                .build());
            return;
        }

        try {
            Long chatId = Long.parseLong(s[1]);
            Long exactUserId = userId.get();

            UserEntity userEntity = userRepository.findById(exactUserId).orElseThrow(() -> new RuntimeException("User with id not found " + exactUserId));
            ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat with id not found " + chatId));

            List<SkipChatNotificationEntity> skipChatNotificationEntities = skipChatNotificationRepository.getSkippedChatEntity(exactUserId, chatId);

            if (!skipChatNotificationEntities.isEmpty()) {
                telegramClient.execute(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Вы уже отписаны от уведомлений из чата \"%s\"".formatted(chatEntity.getChatName()))
                    .build());
                return;
            }

            SkipChatNotificationEntity skipChatNotificationEntity = new SkipChatNotificationEntity();
            skipChatNotificationEntity.setChat(chatEntity);
            skipChatNotificationEntity.setUser(userEntity);

            SkipChatNotificationEntity save = skipChatNotificationRepository.save(skipChatNotificationEntity);

            telegramClient.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Успешно отписаны от уведомлений из чата \"%s\"".formatted(save.getChat().getChatName()))
                .build());
        } catch (Exception ex) {
            telegramClient.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Произошла ошибка: " + ExceptionUtils.getMessage(ex))
                .build());
        }
    }
}
