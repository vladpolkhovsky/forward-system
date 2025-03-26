package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.ui.UpdateOrderRequestDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderStatusEntity;
import by.forward.forward_system.core.jpa.model.UpdateOrderRequestEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.OrderStatusRepository;
import by.forward.forward_system.core.jpa.repository.UpdateOrderRequestRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UpdateRequestOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final UpdateOrderRequestRepository updateOrderRequestRepository;
    private final UserRepository userRepository;

    public UpdateRequestOrderService(OrderRepository orderRepository, OrderStatusRepository orderStatusRepository, UpdateOrderRequestRepository updateOrderRequestRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.updateOrderRequestRepository = updateOrderRequestRepository;
        this.userRepository = userRepository;
    }

    public Integer countUpdateRequests() {
        return updateOrderRequestRepository.countAllByIsViewedFalse();
    }

    public UpdateOrderRequestDto create(MultiValueMap<String, String> multiValueMap,
                                        Long orderId,
                                        BigDecimal orderTechNumber,
                                        OrderStatus newStatus) {

        List<Long> hosts = mapIds(multiValueMap.get("host"));
        List<Long> catchers = mapIds(multiValueMap.get("catcher"));
        List<Long> authors = mapIds(multiValueMap.get("author"));

        boolean isForwardOrder = Optional.ofNullable(multiValueMap.get("isForwardOrder")).isPresent();

        if (CollectionUtils.isEmpty(hosts) && !isForwardOrder) {
            throw new RuntimeException("Не назначен хост.");
        }

        if (CollectionUtils.isEmpty(catchers)) {
            throw new RuntimeException("Не назначен кетчер.");
        }

        if (CollectionUtils.isEmpty(authors)) {
            throw new RuntimeException("Не назначен автор.");
        }

        if ((hosts.size() != 1 && !isForwardOrder) || authors.size() != 1 || catchers.size() != 1) {
            throw new RuntimeException("Неправильный формат данных: " + Map.of("Кетчеры", catchers, "Хосты", hosts, "Прямой заказ", isForwardOrder, "Авторы", authors));
        }

        UpdateOrderRequestDto updateOrderRequestDto = new UpdateOrderRequestDto();

        Long catcherId = catchers.get(0);

        updateOrderRequestDto.setFromUserId(catcherId);
        updateOrderRequestDto.setOrderId(orderId);
        updateOrderRequestDto.setOrderTechNumber(orderTechNumber);
        updateOrderRequestDto.setCreatedAt(LocalDateTime.now());

        updateOrderRequestDto.setNewStatus(newStatus.getName());
        updateOrderRequestDto.setNewStatusRus(newStatus.getRusName());

        updateOrderRequestDto.setCatchers(catchers);
        updateOrderRequestDto.setHosts(hosts);
        updateOrderRequestDto.setAuthors(authors);

        updateOrderRequestDto.setIsViewed(false);
        updateOrderRequestDto.setIsAccepted(null);
        updateOrderRequestDto.setIsForwardOrder(isForwardOrder);

        return updateOrderRequestDto;
    }

    public UpdateOrderRequestDto save(UpdateOrderRequestDto updateOrderRequestDto) {
        UpdateOrderRequestEntity entity = toEntity(updateOrderRequestDto);
        return toDto(updateOrderRequestRepository.save(entity));
    }

    public UpdateOrderRequestDto update(Long requestId, UpdateOrderRequestDto updateOrderRequestDto, Boolean isViewed, Boolean isAccepted) {
        UpdateOrderRequestEntity updateOrderRequestEntity = updateOrderRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("UpdateOrderRequest not found"));

        Long oldFromUserId = updateOrderRequestEntity.getUser().getId();

        UserEntity userEntity = userRepository.findById(oldFromUserId).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderEntity orderEntity = orderRepository.findById(updateOrderRequestDto.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderStatusEntity orderStatus = orderStatusRepository.findById(updateOrderRequestDto.getNewStatus()).orElseThrow(() -> new RuntimeException("New status not found"));

        updateOrderRequestEntity.setOrder(orderEntity);
        updateOrderRequestEntity.setNewStatus(orderStatus);
        updateOrderRequestEntity.setCatcherIds(toStrList(updateOrderRequestDto.getCatchers()));
        updateOrderRequestEntity.setAuthorsIds(toStrList(updateOrderRequestDto.getAuthors()));
        updateOrderRequestEntity.setHostsIds(toStrList(updateOrderRequestDto.getHosts()));
        updateOrderRequestEntity.setIsForwardOrder(updateOrderRequestDto.getIsForwardOrder());
        updateOrderRequestEntity.setCreatedAt(updateOrderRequestDto.getCreatedAt());

        updateOrderRequestEntity.setUser(userEntity);
        updateOrderRequestEntity.setIsViewed(isViewed);
        updateOrderRequestEntity.setIsAccepted(isAccepted);

        UpdateOrderRequestEntity save = updateOrderRequestRepository.save(updateOrderRequestEntity);

        return toDto(save);
    }

    private List<Long> mapIds(List<String> strIds) {
        if (CollectionUtils.isEmpty(strIds)) {
            return List.of();
        }
        return strIds.stream().map(Long::parseLong).toList();
    }

    private List<Long> mapIds(String strIds) {
        if (StringUtils.isBlank(strIds)) {
            return List.of();
        }
        String[] split = strIds.split(",");
        return mapIds(Arrays.stream(split).toList());
    }

    private String toStrList(List<Long> ids) {
        return String.join(",", ids.stream().map(Object::toString).toList());
    }

    public UpdateOrderRequestEntity toEntity(UpdateOrderRequestDto updateOrderRequestDto) {
        OrderEntity orderEntity = orderRepository.findById(updateOrderRequestDto.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        UserEntity userEntity = userRepository.findById(updateOrderRequestDto.getFromUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        OrderStatusEntity orderStatus = orderStatusRepository.findById(updateOrderRequestDto.getNewStatus()).orElseThrow(() -> new RuntimeException("New status not found"));

        UpdateOrderRequestEntity updateOrderRequestEntity = new UpdateOrderRequestEntity();

        updateOrderRequestEntity.setOrder(orderEntity);
        updateOrderRequestEntity.setUser(userEntity);
        updateOrderRequestEntity.setCatcherIds(toStrList(updateOrderRequestDto.getCatchers()));
        updateOrderRequestEntity.setAuthorsIds(toStrList(updateOrderRequestDto.getAuthors()));
        updateOrderRequestEntity.setHostsIds(toStrList(updateOrderRequestDto.getHosts()));
        updateOrderRequestEntity.setNewStatus(orderStatus);
        updateOrderRequestEntity.setIsViewed(updateOrderRequestDto.getIsViewed());
        updateOrderRequestEntity.setIsAccepted(updateOrderRequestDto.getIsAccepted());
        updateOrderRequestEntity.setIsForwardOrder(updateOrderRequestDto.getIsForwardOrder());
        updateOrderRequestEntity.setCreatedAt(updateOrderRequestDto.getCreatedAt());

        return updateOrderRequestEntity;
    }

    public UpdateOrderRequestDto toDto(UpdateOrderRequestEntity updateOrderRequestEntity) {
        UpdateOrderRequestDto updateOrderRequestDto = new UpdateOrderRequestDto();

        updateOrderRequestDto.setId(updateOrderRequestEntity.getId());
        updateOrderRequestDto.setFromUserId(updateOrderRequestEntity.getUser().getId());
        updateOrderRequestDto.setOrderId(updateOrderRequestEntity.getOrder().getId());
        updateOrderRequestDto.setOrderTechNumber(new BigDecimal(updateOrderRequestEntity.getOrder().getTechNumber()));
        updateOrderRequestDto.setAuthors(mapIds(updateOrderRequestEntity.getAuthorsIds()));
        updateOrderRequestDto.setCatchers(mapIds(updateOrderRequestEntity.getCatcherIds()));
        updateOrderRequestDto.setHosts(mapIds(updateOrderRequestEntity.getHostsIds()));
        updateOrderRequestDto.setNewStatus(updateOrderRequestEntity.getNewStatus().getStatus().getName());
        updateOrderRequestDto.setNewStatusRus(updateOrderRequestEntity.getNewStatus().getStatus().getRusName());
        updateOrderRequestDto.setIsViewed(updateOrderRequestEntity.getIsViewed());
        updateOrderRequestDto.setIsAccepted(updateOrderRequestEntity.getIsAccepted());
        updateOrderRequestDto.setIsForwardOrder(updateOrderRequestEntity.getIsForwardOrder());
        updateOrderRequestDto.setCreatedAt(updateOrderRequestEntity.getCreatedAt());

        return updateOrderRequestDto;
    }

    public List<UpdateOrderRequestDto> getNotReviewedOrderRequests() {
        List<UpdateOrderRequestEntity> notReviewedOrderRequests = updateOrderRequestRepository.getNotReviewedOrderRequests();
        return notReviewedOrderRequests.stream().map(this::toDto).toList();
    }

    public UpdateOrderRequestDto getById(Long requestId) {
        UpdateOrderRequestEntity updateOrderRequestEntity = updateOrderRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        return toDto(updateOrderRequestEntity);
    }
}
