package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ForwardOrderChatInfo;
import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.jpa.repository.CustomerTelegramToForwardOrderRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ForwardOrderMapper {
    @Autowired
    private CustomerTelegramToForwardOrderRepository forwardOrderRepository;

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "forwardOrderId", source = "id")
    @Mapping(target = "botCount", source = "id", qualifiedByName = "getForwardOrderBotCount")
    public abstract V3ForwardOrderChatInfo map(ForwardOrderEntity entity);

    @Named("getForwardOrderBotCount")
    public Long getForwardOrderBotCount(Long forwardOrderId) {
        return forwardOrderRepository.countForwardOrderCustomers(forwardOrderId);
    }
}
