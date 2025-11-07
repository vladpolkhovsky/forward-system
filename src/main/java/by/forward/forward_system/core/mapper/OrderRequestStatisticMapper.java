package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.admin.OrderRequestLogDto;
import by.forward.forward_system.core.jpa.repository.OrderRequestStatisticRepository.InfoProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderRequestStatisticMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    OrderRequestLogDto map(InfoProjection projection);
}
