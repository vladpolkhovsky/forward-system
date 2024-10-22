package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ChatNewMessageProjection;
import by.forward.forward_system.core.jpa.repository.projections.ChatProjection;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatMemberService chatMemberService;

    private final MessageService messageService;

    private final ChatMessageToUserRepository chatMessageToUserRepository;

    private final ChatMessageTypeRepository chatMessageTypeRepository;

    private final WebsocketMassageService websocketMassageService;

    private final UserRepository userRepository;

    private final UserService userService;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatTypeRepository chatTypeRepository;
    private final ChatMessageAttachmentRepository chatMessageAttachmentRepository;
    private final ChatMessageOptionRepository chatMessageOptionRepository;
    private final MessageRepository messageRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;

    @Transactional
    public ChatEntity createChat(List<UserEntity> chatMembers, String chatName, OrderEntity orderEntity, String initMessage, ChatTypeEntity chatTypeEntity) {
        ChatEntity chatEntity = new ChatEntity();

        chatEntity.setChatName(chatName);
        chatEntity.setOrder(orderEntity);
        chatEntity.setChatType(chatTypeEntity);
        chatEntity.setLastMessageDate(LocalDateTime.now());

        chatEntity = chatRepository.save(chatEntity);

        for (UserEntity chatMember : chatMembers) {
            ChatMemberEntity chatMemberEntity = chatMemberService.addMemberToChat(chatMember, chatEntity);
            chatEntity.getChatMembers().add(chatMemberEntity);
        }

//        List<UserEntity> admins = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
//        for (UserEntity chatMember : admins) {
//            ChatMemberEntity chatMemberEntity = chatMemberService.addMemberToChat(chatMember, chatEntity);
//            chatEntity.getChatMembers().add(chatMemberEntity);
//        }

        chatEntity = chatRepository.save(chatEntity);

        if (initMessage != null) {
            ChatMessageTypeEntity messageType = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName()).orElseThrow(() -> new RuntimeException("Message type not found"));

            messageService.sendMessage(
                null,
                chatEntity,
                initMessage,
                true,
                messageType,
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        ChatEntity save = chatRepository.save(chatEntity);

        websocketMassageService.notifyNewChatCreated(
            chatEntity.getChatMembers().stream().map(ChatMemberEntity::getUser).map(UserEntity::getId).toList(),
            save.getId()
        );

        return save;
    }

    public ChatDto getChat(Long chatId) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));
        return getChat(chatEntity);
    }

    public Set<Long> getChatMembers(Long chatId) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));

        HashSet<Long> ids = new HashSet<>();
        for (ChatMemberEntity chatMember : chatEntity.getChatMembers()) {
            ids.add(chatMember.getUser().getId());
        }

        return ids;
    }

    public ChatDto getChat(ChatEntity chatEntity) {
        ChatDto chatDto = new ChatDto();
        chatDto.setId(chatEntity.getId());
        chatDto.setChatName(chatEntity.getChatName());
        chatDto.setChatType(chatEntity.getChatType().getType().getName());
        chatDto.setChatTypeRus(chatEntity.getChatType().getType().getRusName());
        chatDto.setOrderId(Optional.ofNullable(chatEntity.getOrder()).map(OrderEntity::getId).orElse(null));
        chatDto.setLastMessageTime(LocalDateTime.from(chatEntity.getLastMessageDate()));

        if (chatEntity.getChatMetadata() != null) {
            chatDto.setOnlyOwnerChat(chatEntity.getChatMetadata().getOwnerTypePermission());
        }

        chatDto.setChatMembers(chatMemberService.getChatMembers(chatEntity));
        chatDto.setMessages(messageService.getChatMessages(chatEntity));
        return chatDto;
    }

    public List<ChatDto> getUserChats(Long userId) {
        List<ChatProjection> chatByUser = chatRepository.findChatByUserProjection(userId);
        List<ChatNewMessageProjection> newMessageProjection = chatRepository.findNewMessageProjection(userId);

        HashMap<Long, Long> idToMassages = new HashMap<>();
        newMessageProjection.forEach(t -> idToMassages.put(t.getChatId(), t.getNotViewedMessageCount()));

        return chatByUser.stream().map(t -> {
            ChatDto chatDto = new ChatDto();
            chatDto.setId(t.getId());
            chatDto.setChatName(t.getChatName());
            chatDto.setChatType(t.getType());
            chatDto.setNotViewedMessagesCount(ObjectUtils.defaultIfNull(idToMassages.get(t.getId()), 0L));
            chatDto.setLastMessageTime(t.getLastMessageDate());
            return chatDto;
        }).toList();
    }

    public ChatDto getUserChat(Long userId, Long chatId) {
        List<ChatEntity> chatByUser = chatRepository.findChatByUserAndChatId(userId, chatId);
        if (chatByUser.isEmpty()) {
            return null;
        } else {
            return getChat(chatByUser.get(0));
        }
    }

    public List<ChatDto> getAdminTalkChats() {
        return chatRepository.findAdminTalkChats().stream()
            .map(this::getChat)
            .toList();
    }

    @Transactional
    public void setMessageViewed(Long chatId, Long userId) {
        List<ChatMessageToUserEntity> allByUserAndOrder = chatMessageToUserRepository.getAllByUserAndOrder(userId, chatId);
        for (ChatMessageToUserEntity chatMessageToUserEntity : allByUserAndOrder) {
            chatMessageToUserEntity.setIsViewed(true);
        }
        chatMessageToUserRepository.saveAll(allByUserAndOrder);
    }

    public boolean isMember(Long chatId, Long userId) {
        return chatRepository.isChatMember(chatId, userId);
    }

    public void addUserToChats(List<Long> chatIds, Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        List<ChatEntity> chats = chatRepository
            .findAllById(chatIds);
        for (ChatEntity chat : chats) {
            chatMemberService.addMemberToChat(userEntity, chat);
        }
    }

    @Transactional
    public void delUserFromChats(List<Long> chatIds, Long userId) {
        List<ChatEntity> allById = chatRepository.findAllById(chatIds);
        for (ChatEntity chatEntity : allById) {
            for (ChatMemberEntity chatMember : chatEntity.getChatMembers()) {
                if (chatMember.getUser().getId().equals(userId)) {
                    chatMemberRepository.delete(chatMember);
                    break;
                }
            }
        }
    }

    public List<ChatDto> getAllChats() {
        return chatRepository.findAllChatsProjection().stream().map(t -> {
            ChatDto chatDto = new ChatDto();
            chatDto.setId(t.getId());
            chatDto.setChatName(t.getChatName());
            chatDto.setChatType(t.getType());
            return chatDto;
        }).toList();
    }

    public void hideMessage(Long messageId) {
        messageService.hideMessage(messageId);
    }

    @Transactional
    public void createChat(String chatName, List<Long> chatMembers) {
        List<UserEntity> allById = userRepository.findAllById(chatMembers);

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.SPECIAL_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));

        createChat(allById, chatName, null, "Чат создан.", chatTypeEntity);
    }

    @Transactional
    public void updateChat(Long chatId, String chatName, List<Long> chatMembers) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));
        chatMemberRepository.deleteAll(chatEntity.getChatMembers());
        chatEntity.getChatMembers().clear();

        chatEntity.setChatName(chatName);
        chatRepository.save(chatEntity);

        for (Long chatMember : chatMembers) {
            addUserToChats(Collections.singletonList(chatId), chatMember);
        }
    }

    @Transactional
    public void setAllMessagesViewed(Long currentUserId) {
        chatMessageToUserRepository.setAllViewed(currentUserId);
    }

    public List<Long> getUsersWithNotViewedMessaged() {
        return chatMessageToUserRepository.getAllNotViewed();
    }

    @Transactional
    public void deleteChat(Long chatId) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));
        chatMemberRepository.deleteAll(chatEntity.getChatMembers());

        List<NotificationOutboxEntity> notificationOutbox = notificationOutboxRepository.findAllByChatId(chatId);
        notificationOutboxRepository.deleteAll(notificationOutbox);

        List<ChatMessageToUserEntity> messagesToUser = chatEntity.getChatMassages().stream().flatMap(t -> t.getChatMessageToUsers().stream()).toList();
        chatMessageToUserRepository.deleteAll(messagesToUser);

        List<ChatMessageAttachmentEntity> chatAttachments = chatEntity.getChatMassages().stream().flatMap(t -> t.getChatMessageAttachments().stream()).toList();
        chatMessageAttachmentRepository.deleteAll(chatAttachments);

        List<ChatMessageOptionEntity> chatMessageOptions = chatEntity.getChatMassages().stream().flatMap(t -> t.getChatMessageOptions().stream()).toList();
        chatMessageOptionRepository.deleteAll(chatMessageOptions);

        chatMessageToUserRepository.deleteAll(chatEntity.getChatMessageToUsers());
        messageRepository.deleteAll(chatEntity.getChatMassages());

        chatRepository.delete(chatEntity);
    }

}
