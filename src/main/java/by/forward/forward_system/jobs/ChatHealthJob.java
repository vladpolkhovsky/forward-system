package by.forward.forward_system.jobs;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.jpa.model.ChatTypeEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository.NotCreatedChatProjection;
import by.forward.forward_system.core.jpa.repository.ChatTypeRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.AuthorService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
public class ChatHealthJob {

    private final ChatRepository chatRepository;
    private final AuthorService authorService;
    private final ChatTypeRepository chatTypeRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 45 */4 * * *")
    @Transactional(readOnly = true)
    public void chatHealthJob() {
        log.info("Начало поиска не созданных чатов");

        ChatTypeEntity type = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).get();
        List<NotCreatedChatProjection> notCreatedChats = chatRepository.checkNotCreatedChats();

        Set<Long> ids = notCreatedChats.stream()
            .flatMap(t -> Stream.of(t.getAuthorId(), t.getManagerId()))
            .collect(Collectors.toSet());

        Map<Long, UserEntity> byId = userRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        notCreatedChats.forEach(notCreatedChat -> {
            UserEntity author = byId.get(notCreatedChat.getAuthorId());
            UserEntity manager = byId.get(notCreatedChat.getManagerId());

            List<UserEntity> chatMembers = List.of(author, manager);
            String newOrderChatName = ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUsername(), manager.getUsername());
            authorService.addChatToUser(chatMembers, newOrderChatName, "Здесь будут появляться новые заказы.\nОжидайте, когда ваш менеджер вышлет вам новый заказ на рассмотрение.",
                type, manager, author);
        });

        log.info("Всего найдено {} не созданных чатов: {}", notCreatedChats.size(), notCreatedChats);
    }
}
