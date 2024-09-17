package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.MessageAttachmentDto;
import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.MessageOptionDto;
import by.forward.forward_system.core.dto.messenger.MessageToUserDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatMessageToUserRepository chatMessageToUserRepository;
    private final ChatMessageOptionRepository chatMessageOptionRepository;
    private final ChatMessageAttachmentRepository chatMessageAttachmentRepository;
    private final AttachmentRepository attachmentRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMessageTypeRepository chatMessageTypeRepository;

    @Transactional
    public ChatMessageEntity sendMessage(UserEntity fromUserEntity,
                                         ChatEntity chatEntity,
                                         String message,
                                         Boolean isSystemMessage,
                                         ChatMessageTypeEntity chatMessageTypeEntity,
                                         List<ChatMessageAttachmentEntity> attachments,
                                         List<ChatMessageOptionEntity> options) {
        LocalDateTime now = LocalDateTime.now();

        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setChat(chatEntity);
        chatMessageEntity.setChatMessageType(chatMessageTypeEntity);
        chatMessageEntity.setContent(message);
        chatMessageEntity.setIsSystemMessage(isSystemMessage);
        chatMessageEntity.setFromUser(fromUserEntity);
        chatMessageEntity.setCreatedAt(now);

        chatMessageEntity = messageRepository.save(chatMessageEntity);

        chatEntity.setLastMessageDate(now);
        chatEntity.getChatMassages().add(chatMessageEntity);

        chatRepository.save(chatEntity);

        for (ChatMessageOptionEntity option : options) {
            option.setMessage(chatMessageEntity);
        }
        options = chatMessageOptionRepository.saveAll(options);

        for (ChatMessageAttachmentEntity attachment : attachments) {
            attachment.setMessage(chatMessageEntity);
        }
        attachments = chatMessageAttachmentRepository.saveAll(attachments);

        chatMessageEntity.getChatMessageOptions().addAll(options);
        chatMessageEntity.getChatMessageAttachments().addAll(attachments);

        chatMessageEntity = messageRepository.save(chatMessageEntity);

        for (ChatMemberEntity chatMember : chatMessageEntity.getChat().getChatMembers()) {
            if (fromUserEntity != null && Objects.equals(chatMember.getUser().getId(), fromUserEntity.getId())) {
                continue;
            }
            ChatMessageToUserEntity chatMessageToUserEntity = new ChatMessageToUserEntity();
            chatMessageToUserEntity.setUser(chatMember.getUser());
            chatMessageToUserEntity.setChat(chatEntity);
            chatMessageToUserEntity.setMessage(chatMessageEntity);
            chatMessageToUserEntity.setIsViewed(false);
            chatMessageToUserEntity.setCreatedAt(now);
            chatMessageToUserEntity = chatMessageToUserRepository.save(chatMessageToUserEntity);
            chatMessageEntity.getChatMessageToUsers().add(chatMessageToUserEntity);
        }

        return messageRepository.save(chatMessageEntity);
    }

    @Transactional
    public MessageDto handleWsMessage(Long chatId, Long userId, String message, List<Long> attachmentIds) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found for id " + chatId));
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found for id " + userId));
        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName()).orElseThrow(() -> new RuntimeException("Chat message type not found for id " + ChatMessageType.MESSAGE.getName()));

        List<AttachmentEntity> attachments = attachmentRepository.findAllById(attachmentIds);
        List<ChatMessageAttachmentEntity> chatMessageAttachmentEntities = new ArrayList<>();
        for (AttachmentEntity attachment : attachments) {
            ChatMessageAttachmentEntity attachmentEntity = new ChatMessageAttachmentEntity();
            attachmentEntity.setAttachment(attachment);
            chatMessageAttachmentEntities.add(attachmentEntity);
        }

        ChatMessageEntity chatMessageEntity = sendMessage(
            userEntity,
            chatEntity,
            message,
            false,
            chatMessageTypeEntity,
            chatMessageAttachmentEntities,
            Collections.emptyList()
        );
        return convertChatMessage(chatMessageEntity);
    }

    public List<MessageDto> getChatMessages(ChatEntity chatEntity) {
        List<MessageDto> messageDtos = new ArrayList<>();
        for (ChatMessageEntity chatMassage : chatEntity.getChatMassages()) {
            messageDtos.add(convertChatMessage(chatMassage));
        }
        messageDtos.sort(Comparator.comparing(MessageDto::getCreatedAt));
        return messageDtos;
    }

    public List<MessageDto> getMessagesFromUser(Long userId) {
        List<ChatMessageEntity> allByUser = messageRepository.findAllByUser(userId);
        return allByUser.stream().map(this::convertChatMessage).toList();
    }

    public MessageDto getMessageById(Long messageId) {
        ChatMessageEntity chatMessageEntity = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found for id " + messageId));
        return convertChatMessage(chatMessageEntity);
    }

    public MessageDto convertChatMessage(ChatMessageEntity chatMessage) {
        MessageDto messageDto = new MessageDto();
        messageDto.setId(chatMessage.getId());
        messageDto.setChatId(chatMessage.getChat().getId());
        messageDto.setChatMessageType(chatMessage.getChatMessageType().getType().getName());
        messageDto.setFromUserId(Optional.ofNullable(chatMessage.getFromUser()).map(UserEntity::getId).orElse(null));
        messageDto.setContent(chatMessage.getContent());
        messageDto.setIsSystemMessage(chatMessage.getIsSystemMessage());
        messageDto.setIsHidden(chatMessage.getIsHidden());
        messageDto.setCreatedAt(LocalDateTime.from(chatMessage.getCreatedAt()));
        messageDto.setOptions(getOptions(chatMessage.getChatMessageOptions()));
        messageDto.setAttachments(getAttachments(chatMessage.getChatMessageAttachments()));
        messageDto.setMessageToUser(getMessageToUser(chatMessage.getChatMessageToUsers()));
        return messageDto;
    }

    private List<MessageToUserDto> getMessageToUser(List<ChatMessageToUserEntity> chatMessageToUsers) {
        List<MessageToUserDto> messageToUserDtos = new ArrayList<>();
        for (ChatMessageToUserEntity chatMessageToUser : chatMessageToUsers) {
            MessageToUserDto messageToUserDto = new MessageToUserDto();

            messageToUserDto.setId(chatMessageToUser.getId());
            messageToUserDto.setUserId(chatMessageToUser.getUser().getId());
            messageToUserDto.setChatId(chatMessageToUser.getChat().getId());
            messageToUserDto.setMessageId(chatMessageToUser.getMessage().getId());
            messageToUserDto.setIsViewed(chatMessageToUser.getIsViewed());
            messageToUserDto.setCreatedAt(chatMessageToUser.getCreatedAt());

            messageToUserDtos.add(messageToUserDto);
        }
        return messageToUserDtos;
    }

    private List<MessageAttachmentDto> getAttachments(List<ChatMessageAttachmentEntity> chatMessageAttachments) {
        List<MessageAttachmentDto> attachmentDtos = new ArrayList<>();
        for (ChatMessageAttachmentEntity chatMessageAttachment : chatMessageAttachments) {
            MessageAttachmentDto messageAttachmentDto = new MessageAttachmentDto();

            messageAttachmentDto.setId(chatMessageAttachment.getId());
            messageAttachmentDto.setAttachmentId(chatMessageAttachment.getAttachment().getId());
            messageAttachmentDto.setMessageId(chatMessageAttachment.getMessage().getId());
            messageAttachmentDto.setAttachmentName(chatMessageAttachment.getAttachment().getFilename());

            attachmentDtos.add(messageAttachmentDto);
        }
        return attachmentDtos;
    }

    private List<MessageOptionDto> getOptions(List<ChatMessageOptionEntity> chatMessageOptions) {
        List<MessageOptionDto> messageOptionDtos = new ArrayList<>();
        for (ChatMessageOptionEntity chatMessageOption : chatMessageOptions) {
            MessageOptionDto messageOptionDto = new MessageOptionDto();

            messageOptionDto.setId(chatMessageOption.getId());
            messageOptionDto.setOptionName(chatMessageOption.getOptionName());
            messageOptionDto.setOptionResolved(chatMessageOption.getOptionResolved());
            messageOptionDto.setContent(chatMessageOption.getContent());
            messageOptionDto.setMessageId(chatMessageOption.getMessage().getId());
            messageOptionDto.setOrderParticipant(chatMessageOption.getOrderParticipant().getType().getName());

            messageOptionDtos.add(messageOptionDto);
        }
        return messageOptionDtos;
    }


    public void hideMessage(Long messageId) {
        ChatMessageEntity chatMessageEntity = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found for id " + messageId));
        chatMessageEntity.setIsHidden(true);
        messageRepository.save(chatMessageEntity);
    }
}
