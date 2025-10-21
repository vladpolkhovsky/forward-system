package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.jpa.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "middleName", source = "surname")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "firstName", source = "firstname")
    UserDto map(UserEntity userEntity);

    List<UserDto> mapMany(List<UserEntity> list);

    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now().toString())")
    @Mapping(target = "roles", source = "authorities")
    by.forward.forward_system.core.dto.auth.UserDto mapToAuthDto(UserEntity byUsername);

}
