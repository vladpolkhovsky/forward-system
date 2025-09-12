package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderDto;
import by.forward.forward_system.core.dto.messenger.v3.V3OrderFullDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.dto.rest.manager.ManagerOrderDto;
import by.forward.forward_system.core.dto.rest.payment.OrderPaymentStatusDto;
import by.forward.forward_system.core.dto.rest.search.OrderSearchCriteria;
import by.forward.forward_system.core.enums.OrderPaymentStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository.OrderIdProjection;
import by.forward.forward_system.core.jpa.repository.projections.OrderChatDataProjection;
import by.forward.forward_system.core.jpa.specs.OrderSpecification;
import by.forward.forward_system.core.mapper.OrderMapper;
import by.forward.forward_system.core.services.core.OrderChatHandlerService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final NewOrderPaymentService newOrderPaymentService;
    private final OrderChatHandlerService chatHandlerService;

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

    @Transactional(readOnly = true)
    public Page<V3OrderFullDto> search(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<OrderEntity> specification = OrderSpecification.buildSearchIdsSpecification(criteria);

        Page<OrderIdProjection> byCriteria = orderRepository.findBy(specification, q -> q.project("id").as(OrderIdProjection.class).page(pageable));

        List<Long> idsByCriteria = byCriteria.map(OrderIdProjection::getId).toList();
        List<OrderEntity> fetchedByIds = orderRepository.findAllByIds(idsByCriteria);

        Map<Long, OrderEntity> orderIdToOrder = fetchedByIds.stream()
            .collect(Collectors.toMap(OrderEntity::getId, Function.identity()));

        Map<Long, OrderChatDataProjection> newMessageCount = chatHandlerService.calcChatData(idsByCriteria,
            AuthUtils.getCurrentUserId());

        return byCriteria
            .map(OrderIdProjection::getId)
            .map(orderIdToOrder::get)
            .map(orderMapper::mapToFullDto)
            .map(order -> Optional.ofNullable(newMessageCount.get(order.getId()))
                .map(data -> order.withOrderChatId(data.getChatId())
                    .withOrderChatIdNewMessages(data.getNewMessageCount())).orElse(order));
    }
}
