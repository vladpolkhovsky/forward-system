package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.dto.rest.manager.ManagerOrderDto;
import by.forward.forward_system.core.dto.rest.payment.OrderPaymentStatusDto;
import by.forward.forward_system.core.enums.OrderPaymentStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final NewOrderPaymentService newOrderPaymentService;

    @Transactional(readOnly = true)
    public List<AuthorOrderDto> getAuthorOrders(Long userId) {
        var orderWhereAuthorIs = orderRepository.getOrderWhereAuthorIs(userId);

        Comparator<OrderEntity> comparator = Comparator.<OrderEntity>comparingInt(oe -> oe.getOrderStatus().getStatus().ordinal())
                .thenComparing(oe -> new BigDecimal(oe.getTechNumber()));

        List<OrderPaymentStatusDto> lastPaymentsStatus = newOrderPaymentService.findLastPaymentsStatus(userId);
        Map<Long, OrderPaymentStatus> orderIdToPaymentStatus = lastPaymentsStatus.stream()
                .collect(Collectors.toMap(t -> t.getOrder().getOrderId(), OrderPaymentStatusDto::getStatus));

        orderWhereAuthorIs.sort(comparator);

        List<AuthorOrderDto> map = orderMapper.map(orderWhereAuthorIs);
        map.forEach(t -> t.setPaymentStatus(orderIdToPaymentStatus.get(t.getOrderId())));

        return map;
    }

    @Transactional(readOnly = true)
    public List<ManagerOrderDto> getManagerOrders(Long userId, Boolean showClosed) {
        var orderWhereManagerIs = orderRepository.getOrderWhereManagerIs(userId, showClosed);

        Comparator<OrderEntity> comparator = Comparator.<OrderEntity>comparingInt(oe -> oe.getOrderStatus().getStatus().ordinal())
            .thenComparing(oe -> new BigDecimal(oe.getTechNumber()));

        orderWhereManagerIs.sort(comparator);

        return orderMapper.mapToManagersDto(orderWhereManagerIs);
    }

    @Transactional(readOnly = true)
    public V3OrderDto getOrderById(Long id) {
        OrderEntity byId = orderRepository.findById(id).get();
        return orderMapper.mapToDto(byId);
    }
}
