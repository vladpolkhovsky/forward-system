package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatFileAttachmentDto;
import by.forward.forward_system.core.jpa.model.ChatAttachmentByChatView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    UserMapper.class, AttachmentMapper.class})
public interface ChatAttachmentByChatViewMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    V3ChatFileAttachmentDto map(ChatAttachmentByChatView attachment);

}
