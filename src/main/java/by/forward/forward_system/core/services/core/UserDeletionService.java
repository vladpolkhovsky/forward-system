package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.auth.Authority;
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

    private final UserRepository userRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final BotNotificationCodeRepository botNotificationCodeRepository;
    private final BotIntegrationDataRepository botIntegrationDataRepository;

    @Transactional
    @SneakyThrows
    public void deleteUser(Long userId) {
        notificationOutboxRepository.deleteAllByUserId(userId);
        botNotificationCodeRepository.deleteById(userId);
        botIntegrationDataRepository.deleteByUserId(userId);

        userRepository.findById(userId).ifPresent(user -> {
            user.setDeleted(true);
            user.addRole(Authority.DELETED);
        });
    }

    @Transactional
    @SneakyThrows
    public void unDeleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setDeleted(false);
            user.removeRole(Authority.DELETED);
        });
    }
}
