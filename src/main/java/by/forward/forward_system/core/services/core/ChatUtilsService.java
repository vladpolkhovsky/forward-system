package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatCreationDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatMetadataDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagMetadataDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.TagCssColor;
import by.forward.forward_system.core.enums.TagType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderParticipantEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.ChatMetadataRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.messager.ChatCreatorService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatUtilsService {

    private final ChatService chatService;
    private final AuthorRepository authorRepository;

    private final UserRepository userRepository;
    private final ChatMetadataRepository chatMetadataRepository;
    private final ChatRepository chatRepository;
    private final ChatCreatorService chatCreatorService;

    public void addToNewsChat(Long userId) {
        if (!chatService.isMember(ChatNames.NEWS_CHAT_ID, userId)) {
            chatService.addUserToChats(List.of(ChatNames.NEWS_CHAT_ID), userId);
        }
    }

    public void addAdminToAllChats(Long id) {
        List<ChatDto> adminTalkChats = chatService.getAllChats();

        List<ChatType> noCheckChatTypes = ChatType.noModerationChatTypes;

        List<Long> chatIds = adminTalkChats.stream()
            .filter(t -> !noCheckChatTypes.contains(ChatType.byName(t.getChatType())))
            .map(ChatDto::getId)
            .toList();

        for (Long chatId : chatIds) {
            if (!chatService.isMember(chatId, id)) {
                chatService.addUserToChats(
                    Collections.singletonList(chatId),
                    id
                );
            }
        }
    }

    @Transactional
    public void createNewOrderChats(Long managerId) {
        UserEntity manager = userRepository.findById(managerId).orElseThrow(() -> new RuntimeException("User not found"));

        List<UserEntity> allAuthors = authorRepository.getNotDeletedAuthors().stream()
            .map(AuthorEntity::getUser)
            .filter(t -> !t.hasAuthority(Authority.BANNED))
            .toList();

        for (UserEntity author : allAuthors) {
            if (chatMetadataRepository.isChatExists(author.getId(), managerId)) {
                continue;
            }

            List<ChatTagDto> tags = createNewOrderChatTags(manager, author);

            ChatCreationDto build = ChatCreationDto.builder()
                .chatName(ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUsername(), manager.getUsername()))
                .members(List.of(author.getId(), manager.getId()))
                .chatType(ChatType.REQUEST_ORDER_CHAT)
                .tags(tags)
                .metadata(ChatMetadataDto.builder()
                    .onlyOwnerCanType(false)
                    .authorCanSubmitFiles(true)
                    .managerId(Optional.of(manager).map(UserEntity::getId).orElse(null))
                    .authorId(Optional.of(author).map(UserEntity::getId).orElse(null))
                    .build())
                .build();

            chatCreatorService.createChatAsync(build, "Здесь будут появляться новые заказы.\nОжидайте, когда ваш менеджер вышлет вам новый заказ на рассмотрение.", true);
        }
    }

    public Long getChatIdWithManagerAndAuthor(Long catcherId, Long authorId) {
        return chatRepository.findChatEntityByUserAndManagerId(authorId, catcherId)
            .orElseThrow(() -> new RuntimeException("Can't find chat with author = %d and manager = %d".formatted(authorId, catcherId)))
            .getId();
    }

    public List<ChatTagDto> createNewOrderChatTags(UserEntity manager, UserEntity author) {
        ChatTagDto newOrderChat = ChatTagDto.builder()
            .name("Новые заказы")
            .metadata(ChatTagMetadataDto.builder()
                .userId(author.getId())
                .type(TagType.SIMPLE)
                .isPersonalTag(false)
                .cssColorName(TagCssColor.secondary)
                .isVisible(false)
                .isPrimaryTag(false)
                .build())
            .build();

        ChatTagDto authorTag = ChatTagDto.builder()
            .name("Автор " + author.getUsername())
            .metadata(ChatTagMetadataDto.builder()
                .userId(author.getId())
                .type(TagType.AUTHOR)
                .isPersonalTag(true)
                .cssColorName(TagCssColor.success)
                .isVisible(true)
                .isPrimaryTag(true)
                .build())
            .build();

        ChatTagDto managerTag = ChatTagDto.builder()
            .name("Менеджер " + manager.getUsername())
            .metadata(ChatTagMetadataDto.builder()
                .userId(manager.getId())
                .type(TagType.MANAGER)
                .isPersonalTag(true)
                .cssColorName(TagCssColor.success)
                .isVisible(true)
                .isPrimaryTag(true)
                .build())
            .build();

        return List.of(newOrderChat, authorTag, managerTag);
    }

    public List<ChatTagDto> createAdminChatTags(UserEntity author) {
        ChatTagDto adminChatTag = ChatTagDto.builder()
            .name("Администрация")
            .metadata(ChatTagMetadataDto.builder()
                .type(TagType.ADMINISTRATION)
                .isPersonalTag(false)
                .cssColorName(TagCssColor.secondary)
                .isVisible(true)
                .isPrimaryTag(false)
                .build())
            .build();

        ChatTagDto authorTag = ChatTagDto.builder()
            .name("Автор " + author.getUsername())
            .metadata(ChatTagMetadataDto.builder()
                .userId(author.getId())
                .type(TagType.AUTHOR)
                .isPersonalTag(true)
                .cssColorName(TagCssColor.success)
                .isVisible(true)
                .isPrimaryTag(true)
                .build())
            .build();

        return List.of(adminChatTag, authorTag);
    }

    public List<ChatTagDto> createOrderChatTags(OrderEntity orderEntity) {
        ChatTagDto orderTag = ChatTagDto.builder()
            .name("ТЗ " + orderEntity.getTechNumber())
            .metadata(ChatTagMetadataDto.builder()
                .type(TagType.ORDER)
                .isPersonalTag(false)
                .cssColorName(TagCssColor.secondary)
                .isVisible(false)
                .isPrimaryTag(true)
                .build())
            .build();

        ChatTagDto discipline = ChatTagDto.builder()
            .name(orderEntity.getDiscipline().getName())
            .metadata(ChatTagMetadataDto.builder()
                .type(TagType.DISCIPLINE)
                .isPersonalTag(false)
                .cssColorName(TagCssColor.secondary)
                .isVisible(false)
                .isPrimaryTag(false)
                .build())
            .build();

        List<ChatTagDto> mainAuthorsTags = orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType() == ParticipantType.MAIN_AUTHOR)
            .map(OrderParticipantEntity::getUser)
            .map(t -> ChatTagDto.builder()
                .name("Автор " + t.getUsername())
                .metadata(ChatTagMetadataDto.builder()
                    .userId(t.getId())
                    .type(TagType.AUTHOR)
                    .isPersonalTag(true)
                    .cssColorName(TagCssColor.success)
                    .isVisible(true)
                    .isPrimaryTag(true)
                    .build())
                .build())
            .toList();

        List<ChatTagDto> hostTags = orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType() == ParticipantType.HOST)
            .map(OrderParticipantEntity::getUser)
            .map(t -> ChatTagDto.builder()
                .name("Менеджер " + t.getUsername())
                .metadata(ChatTagMetadataDto.builder()
                    .userId(t.getId())
                    .type(TagType.HOST)
                    .isPersonalTag(true)
                    .cssColorName(TagCssColor.success)
                    .isVisible(true)
                    .isPrimaryTag(true)
                    .build())
                .build())
            .toList();

        return ListUtils.union(hostTags, ListUtils.union(mainAuthorsTags, List.of(orderTag, discipline)));
    }
}
