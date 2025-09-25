package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupDto;
import by.forward.forward_system.core.jpa.model.CalendarGroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CalendarGroupMapper {

    @Mapping(target = "activeOrders", ignore = true)
    CalendarGroupDto map(CalendarGroupEntity calendarGroupEntity);

    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    CalendarGroupEntity map(CalendarGroupDto calendarGroupDto);
}
