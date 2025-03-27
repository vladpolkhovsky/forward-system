package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.services.messager.ChatService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
public class UserDeletionService {

    private final ChatMetadataRepository chatMetadataRepository;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final BotNotificationCodeRepository botNotificationCodeRepository;
    private final BotIntegrationDataRepository botIntegrationDataRepository;

    @Transactional
    @SneakyThrows
    public void deleteUser(Long userId) {
        List<Long> chatsByUserId = chatMetadataRepository.findChatsByUserId(userId);

        for (Long chatId : chatsByUserId) {
            chatService.deleteChat(chatId);
        }

        notificationOutboxRepository.deleteAllByUserId(userId);
        botNotificationCodeRepository.deleteById(userId);
        botIntegrationDataRepository.deleteByUserId(userId);

        userRepository.findById(userId).ifPresent(user -> {
            user.setUsername("Пользователь удалён (id=%d, username=\"%s\")".formatted(user.getId(), user.getUsername()));
            user.setDeleted(true);
            userRepository.save(user);
        });
    }
}
