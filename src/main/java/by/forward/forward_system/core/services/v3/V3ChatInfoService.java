package by.forward.forward_system.core.services.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ChatOrderInfoDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ForwardOrderChatInfo;
import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3OrderReviewDto;
import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.ReviewRepository;
import by.forward.forward_system.core.mapper.ForwardOrderMapper;
import by.forward.forward_system.core.mapper.OrderMapper;
import by.forward.forward_system.core.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class V3ChatInfoService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ForwardOrderRepository forwardOrderRepository;
    private final ForwardOrderMapper forwardOrderMapper;

    @Transactional(readOnly = true)
    public V3ChatOrderInfoDto loadOrderInfo(Long orderId) {
        return orderRepository.findById(orderId).map(orderMapper::mapToChatOrderInfo).get();
    }

    @Transactional(readOnly = true)
    public List<V3OrderReviewDto> loadOrderReviewsInfo(Long orderId) {
        List<ReviewEntity> reviews = reviewRepository.fetchReviewByOrderId(orderId);
        return reviewMapper.mapMany(reviews);
    }

    @Transactional(readOnly = true)
    public V3ForwardOrderChatInfo loadForwardOrderInfo(Long orderId) {
        Optional<ForwardOrderEntity> byOrderId = forwardOrderRepository.findByOrder_Id(orderId);
        return forwardOrderMapper.map(byOrderId.get());
    }
}
