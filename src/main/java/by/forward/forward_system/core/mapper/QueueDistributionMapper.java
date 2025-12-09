package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.DistributionItemLogDto;
import by.forward.forward_system.core.dto.messenger.v3.DistributionLogDto;
import by.forward.forward_system.core.jpa.model.QueueDistributionEntity;
import by.forward.forward_system.core.jpa.model.QueueDistributionItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    UserMapper.class
})
public interface QueueDistributionMapper {

    List<DistributionLogDto> mapMany(List<QueueDistributionEntity> list);

    List<DistributionItemLogDto> mapItemMany(List<QueueDistributionItemEntity> list);

    @Mapping(target = "statusTypeRus", source = "status.rusName")
    @Mapping(target = "statusType", source = "status")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "startTimeAt", source = "startTimeAt", dateFormat = "dd.MM.yyyy HH:mm")
    DistributionLogDto mapToLog(QueueDistributionEntity entity);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "statusTypeRus", source = "status.rusName")
    @Mapping(target = "statusType", source = "status")
    @Mapping(target = "waitUntil", source = "waitUntil", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "waitStart", source = "waitStart", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    DistributionItemLogDto mapToItemDto(QueueDistributionItemEntity entity);
}
