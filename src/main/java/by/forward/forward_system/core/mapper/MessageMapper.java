package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3AttachmentDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3MessageDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3NewMessageDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3OptionDto;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.utils.AuthUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    imports = {LocalDateTime.class},
    uses = {DisplayTimeMapper.class, ChatNameMapper.class}
)
public interface MessageMapper {

    @Mapping(target = "message", source = "content")
    @Mapping(target = "fromUserUsername", source = "fromUser.username")
    @Mapping(target = "fromUserId", source = "fromUser.id")
    @Mapping(target = "chatType", source = "chat.chatType.type")
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "chatName", source = "chat", qualifiedByName = "calculateDisplayName")
    V3NewMessageDto mapToNewMessage(ChatMessageEntity message);

    List<V3NewMessageDto> mapToNewMessage(List<ChatMessageEntity> chatEntities);

    @Mapping(target = "text", source = "content")
    @Mapping(target = "realCreatedAt", source = "createdAt")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toDisplayableString")
    @Mapping(target = "options", source = "chatMessageOptionsSet")
    @Mapping(target = "messageType", source = "chatMessageType.type")
    @Mapping(target = "messageReadedByUsernames", expression = "java(whoReadMessage(message.getChat(), message))")
    @Mapping(target = "isNewMessage", source = "message", qualifiedByName = "isNewMessageForUser")
    @Mapping(target = "fromUserId", source = "fromUser.id")
    @Mapping(target = "fromUserUsername", source = "fromUser.username")
    @Mapping(target = "fromUserIsAdmin", source = "fromUser.authorities", qualifiedByName = "isAdmin")
    @Mapping(target = "fromUserIsDeleted", source = "fromUser.authorities", qualifiedByName = "isDeleted")
    @Mapping(target = "fromUserOrderParticipantType", source = "message", qualifiedByName = "getOrderParticipantType")
    @Mapping(target = "fromUserOrderParticipantTypeRusName", source = "message", qualifiedByName = "getOrderParticipantTypeRus")
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "attachments", source = "chatMessageAttachmentsSet")
    V3MessageDto map(ChatMessageEntity message);

    List<V3MessageDto> map(List<ChatMessageEntity> messages);

    default List<String> whoReadMessage(ChatEntity chatEntity, ChatMessageEntity messageEntity) {
        Set<UserEntity> participants = chatEntity.getParticipants();

        if (participants.size() > 20) {
            return List.of("Немозможно отобразить.");
        }

        Map<Long, Boolean> userIdToIsViewed = messageEntity.getChatMessageToUsersSet().stream()
            .collect(Collectors.toMap(t -> t.getUser().getId(), ChatMessageToUserEntity::getIsViewed));

        return participants.stream()
            .filter(t -> Optional.ofNullable(userIdToIsViewed.get(t.getId())).orElse(true))
            .map(UserEntity::getUsername)
            .toList();
    }

    @Named("getOrderParticipantType")
    default String getOrderParticipantType(ChatMessageEntity message) {
        if (message.getFromUser() == null) {
            return null;
        }

        ParticipantType participantType = Optional.ofNullable(message.getChat().getOrder())
            .map(OrderEntity::getOrderParticipants)
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(k -> k.getUser().getId(), v -> v.getParticipantsType().getType(), defineMainParticipantType()))
            .get(message.getFromUser().getId());

        return Optional.ofNullable(participantType).map(ParticipantType::getName).orElse(null);
    }

    @Mapping(target = "id", source = "attachment.id")
    @Mapping(target = "name", source = "attachment.filename")
    V3AttachmentDto map(ChatMessageAttachmentEntity attachment);

    @Mapping(target = "isResolved", source = "optionResolved")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "optionName")
    @Mapping(target = "url", source = "content")
    V3OptionDto map(ChatMessageOptionEntity option);

    @Named("getOrderParticipantTypeRus")
    default String getOrderParticipantTypeRus(ChatMessageEntity message) {
        if (message.getFromUser() == null) {
            return null;
        }

        ParticipantType participantType = Optional.ofNullable(message.getChat().getOrder())
            .map(OrderEntity::getOrderParticipants)
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(k -> k.getUser().getId(), v -> v.getParticipantsType().getType(), defineMainParticipantType()))
            .get(message.getFromUser().getId());

        return Optional.ofNullable(participantType).map(ParticipantType::getRusName).orElse(null);
    }

    @Named("isNewMessageForUser")
    default Boolean isNewMessageForUser(ChatMessageEntity messageEntity) {
        Map<Long, Boolean> userIdToIsViewed = messageEntity.getChatMessageToUsers().stream()
            .collect(Collectors.toMap(t -> t.getUser().getId(), ChatMessageToUserEntity::getIsViewed));
        return !Optional.ofNullable(userIdToIsViewed.get(AuthUtils.getCurrentUserId())).orElse(true);
    }

    @Named("isAdmin")
    default Boolean isAdmin(Set<Authority> authorities) {
        if (authorities == null) {
            return false;
        }
        return authorities.contains(Authority.ADMIN) || authorities.contains(Authority.OWNER);
    }

    @Named("isDeleted")
    default Boolean isDeleted(Set<Authority> authorities) {
        if (authorities == null) {
            return false;
        }
        return authorities.contains(Authority.DELETED);
    }

    default BinaryOperator<ParticipantType> defineMainParticipantType() {
        return (a, b) -> {
            if (CollectionUtils.containsAny(List.of(ParticipantType.MAIN_AUTHOR), a, b)) {
                return ParticipantType.MAIN_AUTHOR;
            }
            if (CollectionUtils.containsAny(List.of(ParticipantType.HOST), a, b)) {
                return ParticipantType.HOST;
            }
            return null;
        };
    }
}
