package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.TagDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.jpa.model.TagEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {

    @Mapping(target = "associatedUser", source = "userId")
    TagEntity mapToEntity(TagDto createTag);

    default UserEntity mapToUserEntity(Long userId) {
        return Optional.ofNullable(userId)
            .map(UserEntity::of)
            .orElse(null);
    }

    @Mapping(target = "userId", source = "metadata.userId")
    @Mapping(target = "type", source = "metadata.type")
    @Mapping(target = "isVisible", source = "metadata.isVisible")
    @Mapping(target = "isPrimary", source = "metadata.isPrimaryTag")
    @Mapping(target = "isPersonal", source = "metadata.isPersonalTag")
    @Mapping(target = "cssColorName", source = "metadata.cssColorName")
    TagDto mapChatTagDto(ChatTagDto chatTagDto);

    List<TagDto> mapFromChatTagToTag(List<ChatTagDto> tags);
}
