package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    AuthorDto map(AuthorEntity author);

}
