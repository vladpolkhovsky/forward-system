package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3AttachmentDto;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttachmentMapper {

    @Mapping(target = "name", source = "filename")
    V3AttachmentDto map(AttachmentEntity attachment);
}
