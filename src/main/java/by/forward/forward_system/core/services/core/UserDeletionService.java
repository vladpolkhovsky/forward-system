package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.repository.ChatMetadataRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
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

    @Transactional
    @SneakyThrows
    public void deleteUser(Long userId) {
        List<Long> chatsByUserId = chatMetadataRepository.findChatsByUserId(userId);

        List<CompletableFuture<Void>> wait = new ArrayList<>();

        for (Long l : chatsByUserId) {
            wait.add(chatService.deleteChat(l));
        }

        for (CompletableFuture<Void> voidCompletableFuture : wait) {
            voidCompletableFuture.get();
        }

        userRepository.findById(userId).ifPresent(user -> {
            user.setUsername("Пользователь удалён (id=%d, username=\"%s\")".formatted(user.getId(), user.getUsername()));
            user.setDeleted(true);
            userRepository.save(user);
        });
    }
}
