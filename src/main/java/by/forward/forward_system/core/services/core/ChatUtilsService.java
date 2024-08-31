package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.ChatTypeEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.ChatTypeRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatUtilsService {

    private final ChatService chatService;

    private final AuthorRepository authorRepository;

    private final UserRepository userRepository;
    private final ChatTypeRepository chatTypeRepository;

    public void addAdminToAllChats(Long id) {
        List<ChatDto> adminTalkChats = chatService.getAllChats();
        List<Long> chatIds = adminTalkChats.stream()
            .map(ChatDto::getId)
            .toList();
        chatService.addUserToChats(
            chatIds,
            id
        );
    }

    public void createNewOrderChats(Long id) {
        UserEntity manager = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        ChatTypeEntity requestOrderChat = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));

        List<UserEntity> allAuthors = authorRepository.findAll().stream()
            .map(AuthorEntity::getUser)
            .filter(t -> !t.getAuthorities().contains(Authority.BANNED))
            .toList();
        for (UserEntity author : allAuthors) {
            chatService.createChat(
                Arrays.asList(manager, author),
                ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUsername(), manager.getUsername()),
                null,
                "Здесь будут появляться новые заказы!",
                requestOrderChat
            );
        }
    }
}
