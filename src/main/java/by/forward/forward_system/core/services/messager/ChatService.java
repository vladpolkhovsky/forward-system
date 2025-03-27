package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.events.events.CheckMessageByAiEvent;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ChatNewMessageProjection;
import by.forward.forward_system.core.jpa.repository.projections.ChatProjection;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import by.forward.forward_system.core.services.newchat.ChatTabToChatTypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
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

    private final ChatMemberRepository chatMemberRepository;

    private final ChatTypeRepository chatTypeRepository;

    private final ChatMessageAttachmentRepository chatMessageAttachmentRepository;

    private final ChatMessageOptionRepository chatMessageOptionRepository;

    private final MessageRepository messageRepository;

    private final NotificationOutboxRepository notificationOutboxRepository;

    private final ChatTabToChatTypeService chatTabToChatTypeService;

    private final SkipChatNotificationRepository skipChatNotificationRepository;

    private final ChatNoteRepository chatNoteRepository;

    private final ChatMetadataRepository chatMetadataRepository;

    private final SavedChatRepository savedChatRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final LastMessageRepository lastMessageRepository;
    private final TaskScheduler taskScheduler;

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
                Collections.emptyList(),
                false
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
        List<ChatMessageToUserEntity> allByUserAndOrder = chatMessageToUserRepository.getAllByUserAndChat(userId, chatId);
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

    @Transactional
    public void setAllMessagesViewed(Long currentUserId, String chatTab) {
        chatMessageToUserRepository.setAllViewed(currentUserId, chatTabToChatTypeService.getChatTypeByTab(chatTab));
    }

    public List<Long> getUsersWithNotViewedMessaged() {
        return chatMessageToUserRepository.getAllNotViewed();
    }

    @Transactional
    public void deleteChat(Long chatId) {
        log.info("Удаляем чат id={}", chatId);

        Optional<ChatEntity> byId = chatRepository.findById(chatId);

        if (byId.isEmpty()) {
            log.info("Нет чат id={}", chatId);
            return;
        }

        ChatEntity chatEntity = byId.get();

        chatMemberRepository.deleteByChatId(chatEntity.getId());
        chatEntity.getChatMembers().clear();

        savedChatRepository.deleteByChatId(chatEntity.getId());
        chatNoteRepository.deleteByChatId(chatEntity.getId());

        skipChatNotificationRepository.deleteByChatId(chatEntity.getId());

        notificationOutboxRepository.deleteByChatId(chatEntity.getId());

        chatMessageToUserRepository.deleteByChatId(chatEntity.getId());
        chatEntity.getChatMessageToUsers().clear();

        chatMessageAttachmentRepository.deleteByChatId(chatEntity.getId());

        chatMessageOptionRepository.deleteByChatId(chatEntity.getId());

        lastMessageRepository.deleteByChatId(chatEntity.getId());

        messageRepository.deleteByChatId(chatEntity.getId());
        chatEntity.getChatMassages().clear();

        chatMetadataRepository.deleteById(chatEntity.getId());

        chatRepository.delete(chatEntity);

        log.info("Удалили чат id={}", chatId);
    }

    @Transactional
    public Long sendMessageViaHttp(Optional<AttachmentEntity> file, String messageText, Long chatId, Long userId) {
        UserEntity userEntity = userRepository.findById(userId).
            orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        ChatEntity chatEntity = chatRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found with id " + chatId));

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        List<ChatMessageAttachmentEntity> attachments = new ArrayList<>();

        file.stream()
            .map(ChatMessageAttachmentEntity::of)
            .forEach(attachments::add);

        ChatMessageEntity message = messageService.sendMessage(
            userEntity,
            chatEntity,
            messageText,
            false,
            chatMessageTypeEntity,
            attachments,
            List.of()
        );

        taskScheduler.schedule(() -> applicationEventPublisher.publishEvent(new CheckMessageByAiEvent(message.getId())), plusSeconds(5));

        return message.getId();
    }

    public Instant plusSeconds(int seconds) {
        return LocalDateTime.now()
            .plusSeconds(seconds)
            .atZone(ZoneId.of("Europe/Moscow"))
            .toInstant();
    }
}
