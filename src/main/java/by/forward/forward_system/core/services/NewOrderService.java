package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public List<AuthorOrderDto> getAuthorOrders(long userId) {
        var orderWhereAuthorIs = orderRepository.getOrderWhereAuthorIs(userId);

        Comparator<OrderEntity> comparator = Comparator.<OrderEntity>comparingInt(oe -> oe.getOrderStatus().getStatus().ordinal())
                .thenComparing(oe -> new BigDecimal(oe.getTechNumber()));

        orderWhereAuthorIs.sort(comparator);

        return orderMapper.map(orderWhereAuthorIs);
    }
}
