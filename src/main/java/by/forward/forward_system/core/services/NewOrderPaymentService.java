package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.payment.CreateOrderPaymentStatusRequest;
import by.forward.forward_system.core.dto.rest.payment.OrderPaymentStatusDto;
import by.forward.forward_system.core.jpa.model.OrderPaymentStatusEntity;
import by.forward.forward_system.core.jpa.repository.OrderPaymentStatusRepository;
import by.forward.forward_system.core.mapper.OrderPaymentStatusMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewOrderPaymentService {

    private final OrderPaymentStatusRepository paymentStatusRepository;
    private final OrderPaymentStatusMapper paymentStatusMapper;
    private final EntityManager entityManager;

    public List<OrderPaymentStatusDto> findLastPaymentsStatus(Long userId) {
        List<Long> ids = paymentStatusRepository.findLastPaymentStatus(userId);
        return paymentStatusMapper.map(paymentStatusRepository.findAllByIds(ids));
    }

    public List<OrderPaymentStatusDto> findPayments(Long userId) {
        List<OrderPaymentStatusEntity> allByUserId = paymentStatusRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing(OrderPaymentStatusEntity::getCreatedAt).reversed())
                .toList();
        return paymentStatusMapper.map(allByUserId);
    }

    @Transactional
    public List<OrderPaymentStatusDto> save(List<CreateOrderPaymentStatusRequest> request) {
        List<OrderPaymentStatusEntity> orderPaymentStatusEntities = paymentStatusMapper.mapRequest(request);
        orderPaymentStatusEntities = paymentStatusRepository.saveAllAndFlush(orderPaymentStatusEntities);

        entityManager.clear();

        List<Long> ids = orderPaymentStatusEntities.stream().map(OrderPaymentStatusEntity::getId).toList();
        return paymentStatusMapper.map(paymentStatusRepository.findAllByIds(ids));
    }
}
