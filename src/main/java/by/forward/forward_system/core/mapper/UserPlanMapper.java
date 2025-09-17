package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.UserPlanViewProjectionDto;
import by.forward.forward_system.core.jpa.repository.PlanRepository.UserPlanProgressProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPlanMapper {

    @Mapping(target = "planStart", source = "planStart", dateFormat = "dd.MM.yyyy")
    @Mapping(target = "planEnd", source = "planEnd", dateFormat = "dd.MM.yyyy")
    UserPlanViewProjectionDto map(UserPlanProgressProjection projection);

    List<UserPlanViewProjectionDto> mapMany(List<UserPlanProgressProjection> projection);
}
