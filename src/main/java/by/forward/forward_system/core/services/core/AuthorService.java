package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.AuthorWithDisciplineProjection;
import by.forward.forward_system.core.services.messager.ChatMemberService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final AuthorDisciplineRepository authorDisciplineRepository;
    private final ChatMetadataRepository chatMetadataRepository;
    private final ChatUtilsService chatUtilsService;

    public Optional<AuthorEntity> getById(Long id) {
        return authorRepository.findById(id);
    }

    @Transactional
    public AuthorEntity save(UserEntity cratedByUser, AuthorEntity author) {
        UserEntity authorUser = author.getUser();

        authorUser = userService.save(authorUser);

        ChatTypeEntity requestOrderChat = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        ChatTypeEntity adminTalkChat = chatTypeRepository.findById(ChatType.ADMIN_TALK_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));

        ChatEntity adminChat = addChatToUser(
            Collections.singletonList(authorUser),
            ChatNames.ADMINISTRATION_CHAT_NAME.formatted(author.getUser().getUsername()),
            "Чат для связи с администраторами!",
            adminTalkChat,
            null,
            authorUser
        );

        chatUtilsService.addToNewsChat(authorUser.getId());

        List<UserEntity> allAdmins = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        for (UserEntity admin : allAdmins) {
            chatMemberService.addMemberToChat(admin, adminChat);
        }

        List<UserEntity> allManagers = userService.findUsersWithRole(Authority.MANAGER.getAuthority());
        for (UserEntity manager : allManagers) {
            ArrayList<UserEntity> chatMembers = new ArrayList<>(allAdmins);
            chatMembers.addAll(Arrays.asList(authorUser, manager));

            addChatToUser(
                chatMembers,
                ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUser().getUsername(), manager.getUsername()),
                "Здесь будут появляться новые заказы.\nОжидайте, когда ваш менеджер вышлет вам новый заказ на рассмотрение.",
                requestOrderChat,
                manager,
                authorUser
            );
        }

        author.setUser(authorUser);
        author.setCreatedByUser(cratedByUser);

        AuthorEntity save = authorRepository.save(author);

        authorDisciplineRepository.saveAll(save.getAuthorDisciplines());

        return save;
    }

    @Transactional
    public ChatEntity addChatToUser(List<UserEntity> users, String chatName, String initMessage, ChatTypeEntity chatTypeEntity, UserEntity manager, UserEntity author) {
        ChatEntity chat = chatService.createChat(users, chatName, null, initMessage, chatTypeEntity);

        if (manager != null || author != null) {
            ChatMetadataEntity chatMetadataEntity = new ChatMetadataEntity();
            chatMetadataEntity.setOwnerTypePermission(false);
            chatMetadataEntity.setManager(manager);
            chatMetadataEntity.setUser(author);
            chatMetadataEntity.setChat(chat);
            chatMetadataRepository.save(chatMetadataEntity);
        }

        return chat;
    }

    @Transactional
    public AuthorEntity update(Long id, AuthorEntity author) {
        Optional<AuthorEntity> byId = authorRepository.findById(id);

        AuthorEntity authorEntity = byId.orElseThrow(() -> new RuntimeException("Author with id " + id + " not found"));

        authorDisciplineRepository.deleteAll(authorEntity.getAuthorDisciplines());
        authorEntity.getAuthorDisciplines().clear();

        if (!StringUtils.isEmpty(author.getUser().getPassword())) {
            authorEntity.getUser().setPassword(passwordEncoder.encode(author.getUser().getPassword()));
        }

        UserEntity save = userService.update(author.getUser().getId(), author.getUser());

        AuthorEntity authorSaved = authorRepository.save(authorEntity);

        authorSaved.getAuthorDisciplines().addAll(author.getAuthorDisciplines());
        for (AuthorDisciplineEntity authorDiscipline : authorSaved.getAuthorDisciplines()) {
            authorDiscipline.setAuthor(authorSaved);
        }

        authorDisciplineRepository.saveAll(authorSaved.getAuthorDisciplines());

        return authorSaved;
    }

    public List<AuthorWithDisciplineProjection> getAllAuthorsWithDiscipline() {
        return authorRepository.findAllWithDisciplines();
    }

    public List<AuthorEntity> getAllAuthors() {
        return authorRepository.findAll();
    }

    public List<AuthorEntity> getAllAuthorsFast() {
        return authorRepository.getAuthorsFast();
    }
}
