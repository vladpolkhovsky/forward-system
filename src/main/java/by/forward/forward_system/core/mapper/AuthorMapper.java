package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorFullDto;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {DisciplineMapper.class})
public interface AuthorMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    AuthorDto map(AuthorEntity author);

    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "activeOrders", ignore = true)
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "maxOrdersCount", constant = "10L")
    @Mapping(target = "disciplines", source = "author")
    AuthorFullDto mapReach(AuthorEntity author);

}
