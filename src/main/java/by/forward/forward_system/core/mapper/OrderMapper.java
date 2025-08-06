package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto.AuthorAdditionalDatesDto;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class OrderMapper {

    private static final TypeReference<List<AuthorAdditionalDatesDto>> typedReference = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderTechNumber", source = "techNumber")
    @Mapping(target = "deadline", source = "deadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "intermediateDeadline", source = "intermediateDeadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "additionalDates", source = "additionalDates", qualifiedByName = "additionalDatesMapper")
    @Mapping(target = "orderStatus", source = "orderStatus.status.name")
    @Mapping(target = "orderStatusRus", source = "orderStatus.status.rusName")
    @Mapping(target = "paymentStatus", ignore = true)
    public abstract AuthorOrderDto map(OrderEntity orderEntity);

    public abstract List<AuthorOrderDto> map(List<OrderEntity> orderEntities);

    @Named("localDataTimeToMMDDYY")
    public String localDataTimeToMMDDYY(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return date.format(outputDateTimeFormat);
    }

    @SneakyThrows
    @Named("additionalDatesMapper")
    public List<AuthorAdditionalDatesDto> additionalDatesMapper(String date) {
        if (StringUtils.isBlank(date)) {
            return List.of();
        }
        return objectMapper.readValue(date, typedReference).stream()
                .map(dto -> dto.withTime(localDataTimeToMMDDYY(LocalDateTime.parse(dto.getTime()))))
                .toList();
    }
}
