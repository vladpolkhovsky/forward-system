package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderReviewDto;
import by.forward.forward_system.core.dto.messenger.v3.V3SearchOrderReviewDto;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.ReviewRepository;
import by.forward.forward_system.core.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewReviewService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public Page<V3SearchOrderReviewDto> search(String techNumber, Pageable pageable) {
        Page<OrderEntity> orders = orderRepository.findOrderIdsByTechNumber(techNumber, pageable);

        List<Long> orderIds = orders.getContent().stream().map(OrderEntity::getId).toList();
        List<ReviewEntity> reviews = reviewRepository.fetchReviewByOrderIdIn(orderIds);
        List<V3OrderReviewDto> reviewDtos = reviewMapper.mapMany(reviews);

        Map<Long, List<V3OrderReviewDto>> groupByOrder = reviewDtos.stream()
            .collect(Collectors.groupingBy(V3OrderReviewDto::getOrderId));

        return orders.map(order -> new V3SearchOrderReviewDto(order.getId(),
            order.getTechNumber(),
            order.getOrderStatus().getStatus(),
            order.getOrderStatus().getStatus().getRusName(),
            groupByOrder.getOrDefault(order.getId(), List.of()).stream()
                .sorted(Comparator.comparing(V3OrderReviewDto::getId).reversed())
                .toList())
        );
    }
}
