package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.ChatTypeEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.ChatTypeRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.messager.ChatMemberService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.ChatNames;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorService {

    private final UserService userService;

    private final AuthorRepository authorRepository;

    private final PasswordEncoder passwordEncoder;

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ChatMemberService chatMemberService;
    private final ChatTypeRepository chatTypeRepository;

    public Optional<AuthorEntity> getById(Long id) {
        return authorRepository.findById(id);
    }

    public AuthorEntity save(UserEntity cratedByUser, AuthorEntity author) {
        UserEntity authorUser = author.getUser();

        authorUser = userService.save(authorUser);

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.OTHER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        addChatToUser(authorUser,
            ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUser().getUsername()),
            "Здесь будут появляться новые заказы!",
            chatTypeEntity);
        ChatEntity adminChat = addChatToUser(authorUser,
            ChatNames.ADMINISTRATION_CHAT_NAME.formatted(author.getUser().getUsername()),
            "Чат для связи с администраторами!",
            chatTypeEntity);

        List<UserEntity> allAdmins = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        for (UserEntity admin : allAdmins) {
            chatMemberService.addMemberToChat(admin, adminChat);
        }

        author.setUser(authorUser);
        author.setCreatedByUser(cratedByUser);

        return authorRepository.save(author);
    }

    private ChatEntity addChatToUser(UserEntity user, String chatName, String initMessage, ChatTypeEntity chatTypeEntity) {
        return chatService.createChat(Collections.singletonList(user), chatName, null, initMessage, chatTypeEntity);
    }

    public AuthorEntity update(Long id, AuthorEntity author) {
        Optional<AuthorEntity> byId = authorRepository.findById(id);
        AuthorEntity authorEntity = byId.orElseThrow(() -> new RuntimeException("Author with id " + id + " not found"));

        if (!StringUtils.isEmpty(author.getUser().getPassword())) {
            authorEntity.getUser().setPassword(passwordEncoder.encode(author.getUser().getPassword()));
        }

        UserEntity save = userService.save(author.getUser());

        authorEntity.setSubjects(author.getSubjects());
        authorEntity.setQuality(author.getQuality());

        return authorRepository.save(authorEntity);
    }

    public List<AuthorEntity> getAllAuthors() {
        return authorRepository.findAll();
    }
}
