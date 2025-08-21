package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.v3.ChatCreationDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatMetadataDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagMetadataDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.TagCssColor;
import by.forward.forward_system.core.enums.TagType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.ChatTypeEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AuthorDisciplineRepository;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.ChatTypeRepository;
import by.forward.forward_system.core.jpa.repository.projections.AuthorWithDisciplineProjection;
import by.forward.forward_system.core.services.messager.ChatCreatorService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthorService {

    private final UserService userService;

    private final AuthorRepository authorRepository;

    private final PasswordEncoder passwordEncoder;

    private final ChatTypeRepository chatTypeRepository;

    private final AuthorDisciplineRepository authorDisciplineRepository;

    private final ChatUtilsService chatUtilsService;

    private final ChatCreatorService chatCreatorService;

    public Optional<AuthorEntity> getById(Long id) {
        return authorRepository.findById(id);
    }

    @Transactional
    public AuthorEntity save(UserEntity cratedByUser, AuthorEntity author) {
        UserEntity authorUser = author.getUser();

        authorUser = userService.save(authorUser);

        ChatTypeEntity requestOrderChat = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        ChatTypeEntity adminTalkChat = chatTypeRepository.findById(ChatType.ADMIN_TALK_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));

        String adminChatName = ChatNames.ADMINISTRATION_CHAT_NAME.formatted(author.getUser().getUsername());
        addChatToUser(List.of(authorUser), adminChatName, "Чат для связи с администраторами!",
            adminTalkChat, null, authorUser);

        chatUtilsService.addToNewsChat(authorUser.getId());

        List<UserEntity> allManagers = userService.findUsersWithRole(Authority.MANAGER.getAuthority());
        for (UserEntity manager : allManagers) {
            List<UserEntity> chatMembers = List.of(authorUser, manager);
            String newOrderChatName = ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUser().getUsername(), manager.getUsername());
            addChatToUser(chatMembers, newOrderChatName, "Здесь будут появляться новые заказы.\nОжидайте, когда ваш менеджер вышлет вам новый заказ на рассмотрение.",
                requestOrderChat, manager, authorUser);
        }

        author.setUser(authorUser);
        author.setCreatedByUser(cratedByUser);

        AuthorEntity save = authorRepository.save(author);
        authorDisciplineRepository.saveAll(save.getAuthorDisciplines());

        return save;
    }

    @Transactional
    public void addChatToUser(List<UserEntity> users, String chatName, String initMessage, ChatTypeEntity chatTypeEntity, UserEntity manager, UserEntity author) {
        List<ChatTagDto> chatTags = null;

        if (chatTypeEntity.getType() == ChatType.REQUEST_ORDER_CHAT) {
            chatTags = chatUtilsService.createNewOrderChatTags(manager, author);
        }

        if (chatTypeEntity.getType() == ChatType.ADMIN_TALK_CHAT) {
            chatTags = chatUtilsService.createAdminChatTags(author);
        }

        ChatCreationDto build = ChatCreationDto.builder()
            .chatName(chatName)
            .members(users.stream().map(UserEntity::getId).collect(Collectors.toList()))
            .chatType(chatTypeEntity.getType())
            .tags(chatTags)
            .metadata(ChatMetadataDto.builder()
                .chatType(chatTypeEntity.getType())
                .onlyOwnerCanType(false)
                .authorCanSubmitFiles(true)
                .managerId(Optional.ofNullable(manager).map(UserEntity::getId).orElse(null))
                .authorId(Optional.ofNullable(author).map(UserEntity::getId).orElse(null))
                .build())
            .build();

        chatCreatorService.createChatAsync(build, initMessage, true);
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
        return authorRepository.getNotDeletedAuthors();
    }

    public List<AuthorEntity> getAllAuthorsFast() {
        return authorRepository.getAuthorsFast();
    }
}
