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
import by.forward.forward_system.core.utils.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    public UpdateOrderRequestDto create(MultiValueMap<String, String> multiValueMap,
                                        Long orderId,
                                        Integer orderTechNumber,
                                        OrderStatus newStatus) {
        UserEntity userEntity = userRepository.findByUsername(AuthUtils.getCurrentUserDetails().getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        UpdateOrderRequestDto updateOrderRequestDto = new UpdateOrderRequestDto();

        updateOrderRequestDto.setFromUserId(userEntity.getId());
        updateOrderRequestDto.setOrderId(orderId);
        updateOrderRequestDto.setOrderTechNumber(orderTechNumber);
        updateOrderRequestDto.setCreatedAt(LocalDateTime.now());

        updateOrderRequestDto.setNewStatus(newStatus.getName());
        updateOrderRequestDto.setNewStatusRus(newStatus.getRusName());

        List<Long> hosts = mapIds(multiValueMap.get("host"));
        List<Long> experts = mapIds(multiValueMap.get("expert"));
        List<Long> catchers = mapIds(multiValueMap.get("catcher"));
        List<Long> authors = mapIds(multiValueMap.get("author"));

        updateOrderRequestDto.setCatchers(catchers);
        updateOrderRequestDto.setHosts(hosts);
        updateOrderRequestDto.setExperts(experts);
        updateOrderRequestDto.setAuthors(authors);

        updateOrderRequestDto.setIsViewed(false);
        updateOrderRequestDto.setIsAccepted(null);

        return updateOrderRequestDto;
    }

    public UpdateOrderRequestDto save(UpdateOrderRequestDto updateOrderRequestDto) {
        UpdateOrderRequestEntity entity = toEntity(updateOrderRequestDto);
        return toDto(updateOrderRequestRepository.save(entity));
    }

    public UpdateOrderRequestDto update(Long requestId, UpdateOrderRequestDto updateOrderRequestDto, Boolean isViewed, Boolean isAccepted) {
        UpdateOrderRequestEntity updateOrderRequestEntity = updateOrderRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("UpdateOrderRequest not found"));

        UserEntity userEntity = userRepository.findById(updateOrderRequestDto.getFromUserId()).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderEntity orderEntity = orderRepository.findById(updateOrderRequestDto.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderStatusEntity orderStatus = orderStatusRepository.findById(updateOrderRequestDto.getNewStatus()).orElseThrow(() -> new RuntimeException("New status not found"));

        updateOrderRequestEntity.setOrder(orderEntity);
        updateOrderRequestEntity.setNewStatus(orderStatus);
        updateOrderRequestEntity.setCatcherIds(toStrList(updateOrderRequestDto.getCatchers()));
        updateOrderRequestEntity.setAuthorsIds(toStrList(updateOrderRequestDto.getAuthors()));
        updateOrderRequestEntity.setExpertsIds(toStrList(updateOrderRequestDto.getExperts()));
        updateOrderRequestEntity.setHostsIds(toStrList(updateOrderRequestDto.getHosts()));
        updateOrderRequestEntity.setIsViewed(updateOrderRequestDto.getIsViewed());
        updateOrderRequestEntity.setIsAccepted(updateOrderRequestDto.getIsAccepted());
        updateOrderRequestEntity.setCreatedAt(updateOrderRequestDto.getCreatedAt());
        updateOrderRequestEntity.setUser(userEntity);
        updateOrderRequestEntity.setIsViewed(isViewed);
        updateOrderRequestEntity.setIsAccepted(isAccepted);

        UpdateOrderRequestEntity save = updateOrderRequestRepository.save(updateOrderRequestEntity);

        return toDto(save);
    }

    private List<Long> mapIds(List<String> strIds) {
        return strIds.stream().map(Long::parseLong).toList();
    }

    private List<Long> mapIds(String strIds) {
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
        updateOrderRequestEntity.setExpertsIds(toStrList(updateOrderRequestDto.getExperts()));
        updateOrderRequestEntity.setHostsIds(toStrList(updateOrderRequestDto.getHosts()));
        updateOrderRequestEntity.setNewStatus(orderStatus);
        updateOrderRequestEntity.setIsViewed(updateOrderRequestDto.getIsViewed());
        updateOrderRequestEntity.setIsAccepted(updateOrderRequestDto.getIsAccepted());
        updateOrderRequestEntity.setCreatedAt(updateOrderRequestDto.getCreatedAt());
        return updateOrderRequestEntity;
    }

    public UpdateOrderRequestDto toDto(UpdateOrderRequestEntity updateOrderRequestEntity) {
        UpdateOrderRequestDto updateOrderRequestDto = new UpdateOrderRequestDto();
        updateOrderRequestDto.setId(updateOrderRequestEntity.getId());
        updateOrderRequestDto.setFromUserId(updateOrderRequestEntity.getUser().getId());
        updateOrderRequestDto.setOrderId(updateOrderRequestEntity.getOrder().getId());
        updateOrderRequestDto.setOrderTechNumber(updateOrderRequestEntity.getOrder().getTechNumber());
        updateOrderRequestDto.setAuthors(mapIds(updateOrderRequestEntity.getAuthorsIds()));
        updateOrderRequestDto.setExperts(mapIds(updateOrderRequestEntity.getExpertsIds()));
        updateOrderRequestDto.setCatchers(mapIds(updateOrderRequestEntity.getCatcherIds()));
        updateOrderRequestDto.setHosts(mapIds(updateOrderRequestEntity.getHostsIds()));
        updateOrderRequestDto.setNewStatus(updateOrderRequestEntity.getNewStatus().getStatus().getName());
        updateOrderRequestDto.setNewStatusRus(updateOrderRequestEntity.getNewStatus().getStatus().getRusName());
        updateOrderRequestDto.setIsViewed(updateOrderRequestEntity.getIsViewed());
        updateOrderRequestDto.setIsAccepted(updateOrderRequestEntity.getIsAccepted());
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
