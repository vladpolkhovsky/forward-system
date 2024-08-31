package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        List<UserEntity> admins = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        for (UserEntity chatMember : admins) {
            ChatMemberEntity chatMemberEntity = chatMemberService.addMemberToChat(chatMember, chatEntity);
            chatEntity.getChatMembers().add(chatMemberEntity);
        }

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

    public ChatDto getChat(ChatEntity chatEntity) {
        ChatDto chatDto = new ChatDto();
        chatDto.setId(chatEntity.getId());
        chatDto.setChatName(chatEntity.getChatName());
        chatDto.setChatType(chatEntity.getChatType().getType().getName());
        chatDto.setChatTypeRus(chatEntity.getChatType().getType().getRusName());
        chatDto.setOrderId(Optional.ofNullable(chatEntity.getOrder()).map(OrderEntity::getId).orElse(null));
        chatDto.setLastMessageTime(LocalDateTime.from(chatEntity.getLastMessageDate()));
        chatDto.setChatMembers(chatMemberService.getChatMembers(chatEntity));
        chatDto.setMessages(messageService.getChatMessages(chatEntity));
        return chatDto;
    }

    public List<ChatDto> getUserChats(Long userId) {
        List<ChatEntity> chatByUser = chatRepository.findChatByUser(userId);
        return chatByUser.stream().map(this::getChat).toList();
    }

    public List<ChatDto> getAdminTalkChats() {
        return chatRepository.findAdminTalkChats().stream()
            .map(this::getChat)
            .toList();
    }

    public void setMessageViewed(Long chatId, Long userId) {
        List<ChatMessageToUserEntity> allByUserAndOrder = chatMessageToUserRepository.getAllByUserAndOrder(userId, chatId);
        for (ChatMessageToUserEntity chatMessageToUserEntity : allByUserAndOrder) {
            chatMessageToUserEntity.setIsViewed(true);
        }
        chatMessageToUserRepository.saveAll(allByUserAndOrder);
    }

    public void addUserToChats(List<Long> chatIds, Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        List<ChatEntity> chats = chatRepository
            .findAllById(chatIds);
        for (ChatEntity chat : chats) {
            chatMemberService.addMemberToChat(userEntity, chat);
        }
    }

    public List<ChatDto> getAllChats() {
        return chatRepository.findAll().stream().map(this::getChat).toList();
    }

    public void hideMessage(Long messageId) {
        messageService.hideMessage(messageId);
    }
}
