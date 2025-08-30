package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderDto;
import by.forward.forward_system.core.dto.messenger.v3.V3ParticipantDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ChatOrderInfoDto;
import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderParticipantEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public abstract class OrderMapper {

    private static final TypeReference<List<AdditionalDateDto>> typedReference = new TypeReference<>() {
    };

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

    @Mapping(target = "participants", source = "orderParticipants")
    @Mapping(target = "orderCost", source = "takingCost")
    @Mapping(target = "orderAuthorCost", source = "authorCost")
    @Mapping(target = "disciplineName", source = "discipline.name")
    @Mapping(target = "disciplineId", source = "discipline.id")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "techNumber", source = "techNumber")
    @Mapping(target = "deadline", source = "deadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "intermediateDeadline", source = "intermediateDeadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "additionalDates", source = "additionalDates", qualifiedByName = "additionalDatesMapper")
    @Mapping(target = "orderStatus", source = "orderStatus.status.name")
    @Mapping(target = "orderStatusRus", source = "orderStatus.status.rusName")
    public abstract V3OrderDto mapToDto(OrderEntity entity);

    @Mapping(target = "typeRusName", source = "participantsType.type.rusName")
    @Mapping(target = "type", source = "participantsType.type")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "user", source = "user")
    public abstract V3ParticipantDto map(OrderParticipantEntity orderParticipantEntity);

    @Mapping(target = "managerUsername", source = "orderEntity", qualifiedByName = "getManager")
    @Mapping(target = "authorUsername", source = "orderEntity", qualifiedByName = "getAuthor")
    @Mapping(target = "disciplineName", source = "discipline.name")
    @Mapping(target = "disciplineId", source = "discipline.id")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "techNumber", source = "techNumber")
    @Mapping(target = "deadline", source = "deadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "intermediateDeadline", source = "intermediateDeadline", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "additionalDates", source = "additionalDates", qualifiedByName = "additionalDatesMapper")
    @Mapping(target = "status", source = "orderStatus.status.name")
    @Mapping(target = "statusRusName", source = "orderStatus.status.rusName")
    public abstract V3ChatOrderInfoDto mapToChatOrderInfo(OrderEntity orderEntity);

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
    public List<AdditionalDateDto> additionalDatesMapper(String date) {
        if (StringUtils.isBlank(date)) {
            return List.of();
        }
        return objectMapper.readValue(date, typedReference).stream()
            .map(dto -> dto.withTime(Optional.ofNullable(dto.getTime())
                .filter(StringUtils::isNoneBlank)
                .map(LocalDateTime::parse)
                .map(this::localDataTimeToMMDDYY)
                .orElse("<Дата не указана>")))
            .toList();
    }

    @Named("getAuthor")
    public String getAuthor(OrderEntity orderEntity) {
        return findByRole(orderEntity.getOrderParticipants(), ParticipantType.MAIN_AUTHOR);
    }

    @Named("getManager")
    public String getManager(OrderEntity orderEntity) {
        return findByRole(orderEntity.getOrderParticipants(), ParticipantType.HOST);
    }

    private static String findByRole(List<OrderParticipantEntity> participants, ParticipantType participantType) {
        if (CollectionUtils.isEmpty(participants)) {
            return null;
        }
        return participants.stream().filter(t -> t.getParticipantsType().getType() == participantType)
            .findAny().map(OrderParticipantEntity::getUser).map(UserEntity::getUsername).orElse(null);
    }
}
