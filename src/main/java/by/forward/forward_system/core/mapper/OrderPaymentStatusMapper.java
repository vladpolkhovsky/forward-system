package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.payment.CreateOrderPaymentStatusRequest;
import by.forward.forward_system.core.dto.rest.payment.OrderPaymentStatusDto;
import by.forward.forward_system.core.jpa.model.OrderPaymentStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        OrderMapper.class,
        AuthorMapper.class
})
public interface OrderPaymentStatusMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    OrderPaymentStatusDto map(OrderPaymentStatusEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order.id", source = "orderId")
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "createdBy.id", expression = "java(by.forward.forward_system.core.utils.AuthUtils.getCurrentUserId())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    OrderPaymentStatusEntity map(CreateOrderPaymentStatusRequest dto);

    List<OrderPaymentStatusEntity> mapRequest(List<CreateOrderPaymentStatusRequest> requests);

    List<OrderPaymentStatusDto> map(List<OrderPaymentStatusEntity> entities);

}
