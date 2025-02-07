package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.ChatMetadataRepository;
import by.forward.forward_system.core.jpa.repository.ChatTypeRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatUtilsService {

    private final ChatService chatService;

    private final AuthorRepository authorRepository;

    private final UserRepository userRepository;
    private final ChatTypeRepository chatTypeRepository;
    private final ChatMetadataRepository chatMetadataRepository;

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
        ChatTypeEntity requestOrderChat = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));

        List<UserEntity> allAuthors = authorRepository.getNotDeletedAuthors().stream()
            .map(AuthorEntity::getUser)
            .filter(t -> !t.getAuthorities().contains(Authority.BANNED))
            .toList();

        List<UserEntity> admins = new ArrayList<>(
            userRepository.findByRolesContainsAndDeletedIsFalse(Authority.ADMIN.getAuthority())
        );

        for (UserEntity author : allAuthors) {
            ArrayList<UserEntity> chatMembers = new ArrayList<>(admins);
            chatMembers.addAll(Arrays.asList(manager, author));

            if (chatMetadataRepository.isChatExists(author.getId(), managerId)) {
                continue;
            }

            ChatEntity chat = chatService.createChat(
                chatMembers,
                ChatNames.NEW_ORDER_CHAT_NAME.formatted(author.getUsername(), manager.getUsername()),
                null,
                "Здесь будут появляться новые заказы.\nОжидайте, когда ваш менеджер вышлет вам новый заказ на рассмотрение.",
                requestOrderChat
            );

            ChatMetadataEntity chatMetadataEntity = new ChatMetadataEntity();
            chatMetadataEntity.setUser(author);
            chatMetadataEntity.setManager(manager);
            chatMetadataEntity.setChat(chat);
            chatMetadataEntity.setOwnerTypePermission(false);

            chatMetadataRepository.save(chatMetadataEntity);
        }
    }
}
