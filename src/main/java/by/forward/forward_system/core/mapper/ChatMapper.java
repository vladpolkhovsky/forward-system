package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.ChatCreationDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatMetadataDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagMetadataDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.jpa.model.*;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    imports = {LocalDateTime.class},
    uses = {DisplayTimeMapper.class, ChatNameMapper.class}
)
public interface ChatMapper {

    @Mapping(target = "lastMessageDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "chatMetadata", source = "metadata")
    @Mapping(target = "order", source = "orderId", qualifiedByName = "nullOrderIfNullId")
    @Mapping(target = "tags", ignore = true)
    ChatEntity mapToDtoForSave(ChatCreationDto chatCreationDto);

    @Mapping(target = "id", source = "chatId")
    @Mapping(target = "ownerTypePermission", source = "onlyOwnerCanType")
    @Mapping(target = "user", source = "authorId", qualifiedByName = "nullUserIfNullId")
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "nullUserIfNullId")
    ChatMetadataEntity mapMetadata(ChatMetadataDto chatMetadataDto);

    @AfterMapping
    default void handleMetadataAssignation(@MappingTarget ChatEntity chat) {
        Optional.ofNullable(chat.getChatMetadata()).ifPresent(chatMetadata -> {
            chatMetadata.setChat(chat);
        });
    }

    @Named("nullUserIfNullId")
    default UserEntity nullUserIfNoId(Long id) {
        return Optional.ofNullable(id).map(UserEntity::of).orElse(null);
    }

    @Named("nullOrderIfNullId")
    default OrderEntity nullOrderIfNullId(Long id) {
        return Optional.ofNullable(id).map(OrderEntity::of).orElse(null);
    }

    default ChatTypeEntity mapChatType(ChatType chatType) {
        return new ChatTypeEntity(chatType.getName());
    }

    @Mapping(target = "isForwardOrderPaid", ignore = true)
    @Mapping(target = "isForwardOrder", ignore = true)
    @Mapping(target = "metadata", source = "chatMetadata")
    @Mapping(target = "type", source = "chatType.type")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderTechNumber", source = "order.techNumber")
    @Mapping(target = "lastMessageDate", source = "lastMessageDate", qualifiedByName = "toDisplayableString")
    @Mapping(target = "realLastMessageDate", source = "lastMessageDate")
    @Mapping(target = "newMessageCount", ignore = true)
    @Mapping(target = "displayName", source = "chatEntity", qualifiedByName = "calculateDisplayName")
    @Mapping(target = "orderStatus", source = "order.orderStatus.status.name")
    @Mapping(target = "orderStatusRus", source = "order.orderStatus.status.rusName")
    V3ChatDto matToV3ChatDto(ChatEntity chatEntity);

    @Mapping(target = "userId", source = "associatedUser.id")
    @Mapping(target = "isPrimaryTag", source = "isPrimary")
    @Mapping(target = "isPersonalTag", source = "isPersonal")
    ChatTagMetadataDto mapChatTagMetadata(TagEntity chatTagMetadataDto);

    @Mapping(target = "metadata", source = "tagEntity")
    ChatTagDto mapChatTagDto(TagEntity tagEntity);

    @Mapping(target = "onlyOwnerCanType", source = "ownerTypePermission")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "chatType", source = "chat.chatType.type")
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "authorId", source = "user.id")
    ChatMetadataDto mapToChatMetadataDto(ChatMetadataEntity chatMetadataEntity);

    List<V3ChatDto> matToV3ChatDtoMany(List<ChatEntity> fetchedChats);


}
