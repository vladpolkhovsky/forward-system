package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.messenger.OrderParticipantDto;
import by.forward.forward_system.core.dto.ui.AuthorWithFeeDto;
import by.forward.forward_system.core.dto.ui.OrderParticipantUiDto;
import by.forward.forward_system.core.dto.ui.OrderUiDto;
import by.forward.forward_system.core.dto.ui.UserSelectionUiDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderParticipantEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.services.core.AuthorService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderUiService {

    private final OrderService orderService;

    private final UserService userService;

    private final AuthorService authorService;

    public OrderUiDto getOrder(Long id) {
        Optional<OrderEntity> byId = orderService.getById(id);
        OrderEntity orderEntity = byId.orElseThrow(() -> new RuntimeException("Order not found with id " + id));
        return toDto(orderEntity);
    }

    public List<OrderUiDto> getAllOrders() {
        return orderService.findAllOrder().stream()
            .map(this::toDto)
            .sorted(Comparator.comparing(OrderUiDto::getId).reversed())
            .toList();
    }

    public List<OrderUiDto> findAllOrdersInStatus(List<String> statuses) {
        return orderService.findAllOrdersInStatus(statuses)
            .stream().map(this::toDto)
            .sorted(Comparator.comparing(OrderUiDto::getId).reversed())
            .toList();
    }

    public OrderUiDto createOrder(OrderUiDto order) {
        OrderEntity entity = toEntity(order);
        entity = orderService.save(entity);
        return toDto(entity);
    }

    public OrderUiDto updateOrder(Long id, OrderUiDto order) {
        OrderEntity entity = toEntity(order);
        entity = orderService.update(id, entity);
        return toDto(entity);
    }

    public Boolean isDistributionStatus(Long orderId) {
        Optional<OrderEntity> byId = orderService.getById(orderId);
        OrderEntity orderEntity = byId.orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        return orderEntity.getOrderStatus().getStatus().equals(OrderStatus.CREATED)
            || orderEntity.getOrderStatus().getStatus().equals(OrderStatus.DISTRIBUTION);
    }

    public OrderStatus getOrderStatus(Long orderId) {
        Optional<OrderEntity> byId = orderService.getById(orderId);
        OrderEntity orderEntity = byId.orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        return orderEntity.getOrderStatus().getStatus();
    }

    public List<UserSelectionUiDto> getUserListWithCatcherMark(Long orderId) {
        List<UserEntity> allManagers = userService.findUsersWithRole(Authority.MANAGER.getAuthority());

        OrderEntity order = orderService.getById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        List<OrderParticipantEntity> orderParticipants = order.getOrderParticipants();
        Optional<OrderParticipantEntity> catcherManager = orderParticipants.stream()
            .filter(t -> t.getParticipantsType().getType().equals(ParticipantType.CATCHER))
            .findAny();

        Long catcherId = catcherManager
            .map(OrderParticipantEntity::getUser)
            .map(UserEntity::getId)
            .orElse(-1L);

        return allManagers.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), catcherId.equals(t.getId())))
            .toList();
    }

    public List<UserSelectionUiDto> getAuthorListWithAuthorMark(Long orderId) {
        List<AuthorEntity> allAuthors = authorService.getAllAuthors();

        OrderEntity order = orderService.getById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        List<OrderParticipantEntity> orderParticipants = order.getOrderParticipants();
        List<OrderParticipantEntity> authorsParticipants = orderParticipants.stream()
            .filter(t -> isAssignedAuthor(t))
            .toList();
        Set<Long> authorIds = authorsParticipants.stream()
            .map(OrderParticipantEntity::getUser)
            .map(UserEntity::getId)
            .collect(Collectors.toSet());

        return allAuthors.stream()
            .map(AuthorEntity::getUser)
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), authorIds.contains(t.getId())))
            .toList();
    }

    public List<UserSelectionUiDto> getAuthorsByOrder(Long orderId) {
        List<AuthorEntity> allAuthors = authorService.getAllAuthors();

        OrderEntity order = orderService.getById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        List<Long> orderParticipants = order.getOrderParticipants().stream()
            .map(OrderParticipantEntity::getUser)
            .map(UserEntity::getId)
            .toList();

        List<UserSelectionUiDto> list = allAuthors.stream()
            .filter(t -> orderParticipants.contains(t.getId()))
            .map(t -> new UserSelectionUiDto(t.getId(), t.getUser().getFio(), t.getUser().getUsername(), false))
            .toList();

        return list;
    }

    public List<UserSelectionUiDto> getAllManagers() {
        List<UserEntity> allManagers = userService.findUsersWithRole(Authority.MANAGER.getAuthority());
        return allManagers.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), false))
            .toList();
    }

    public List<UserSelectionUiDto> getAllAuthors() {
        List<UserEntity> allManagers = userService.findUsersWithRole(Authority.AUTHOR.getAuthority());
        return allManagers.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), false))
            .toList();
    }

    public List<OrderParticipantUiDto> getAllParticipants(Long orderId) {
        OrderEntity orderEntity = orderService.getById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        List<OrderParticipantUiDto> participantUiDtos = new ArrayList<>();
        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            OrderParticipantUiDto orderParticipantDto = new OrderParticipantUiDto();
            orderParticipantDto.setId(orderParticipant.getId());
            orderParticipantDto.setUserName(orderParticipant.getUser().getUsername());
            orderParticipantDto.setFio(orderParticipant.getUser().getFio());
            orderParticipantDto.setParticipantType(orderParticipant.getParticipantsType().getType().getName());
            orderParticipantDto.setParticipantTypeRus(orderParticipant.getParticipantsType().getType().getRusName());
            orderParticipantDto.setFee(orderParticipant.getFee());
            orderParticipantDto.setHasFee(orderParticipant.getParticipantsType().getType().equals(ParticipantType.MAIN_AUTHOR));
            participantUiDtos.add(orderParticipantDto);
        }
        return participantUiDtos;
    }

    private boolean isAssignedAuthor(OrderParticipantEntity t) {
        return t.getParticipantsType().getType().equals(ParticipantType.AUTHOR)
            || t.getParticipantsType().getType().equals(ParticipantType.DECLINE_AUTHOR);
    }

    private OrderUiDto toDto(OrderEntity orderEntity) {
        return new OrderUiDto(
            orderEntity.getId(),
            orderEntity.getTechNumber(),
            orderEntity.getName(),
            orderEntity.getWorkType(),
            orderEntity.getDiscipline(),
            orderEntity.getSubject(),
            orderEntity.getOriginality(),
            orderEntity.getOrderStatus().getStatus().getName(),
            orderEntity.getOrderStatus().getStatus().getRusName(),
            orderEntity.getVerificationSystem(),
            orderEntity.getIntermediateDeadline().toLocalDate(),
            orderEntity.getDeadline().toLocalDate(),
            orderEntity.getOther(),
            orderEntity.getTakingCost(),
            orderEntity.getAuthorCost(),
            orderEntity.getCreatedAt()
        );
    }

    private OrderEntity toEntity(OrderUiDto orderUiDto) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderUiDto.getId());
        orderEntity.setName(orderUiDto.getName());
        orderEntity.setTechNumber(orderUiDto.getTechNumber());
        orderEntity.setWorkType(orderUiDto.getWorkType());
        orderEntity.setDiscipline(orderUiDto.getDiscipline());
        orderEntity.setSubject(orderUiDto.getSubject());
        orderEntity.setOriginality(orderUiDto.getOriginality());
        orderEntity.setVerificationSystem(orderUiDto.getVerificationSystem());
        orderEntity.setIntermediateDeadline(orderUiDto.getIntermediateDeadline().atStartOfDay());
        orderEntity.setDeadline(orderUiDto.getDeadline().atStartOfDay());
        orderEntity.setOther(orderUiDto.getOther());
        orderEntity.setTakingCost(orderUiDto.getTakingCost());
        orderEntity.setAuthorCost(orderUiDto.getAuthorCost());
        orderEntity.setCreatedAt(orderUiDto.getCreatedAt());
        return orderEntity;
    }

    public List<ChatAttachmentProjection> getOrderMainChatAttachments(Long orderId) {
        return orderService.getOrderMainChatAttachments(orderId);
    }

    public List<AuthorWithFeeDto> getOrderAuthorsWithFee(Long orderId) {
        OrderEntity order = orderService.getById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        List<AuthorWithFeeDto> authorWithFeeDtos = new ArrayList<>();
        for (OrderParticipantEntity participant : order.getOrderParticipants()) {
            if (!participant.getParticipantsType().getType().equals(ParticipantType.MAIN_AUTHOR)) {
                continue;
            }
            authorWithFeeDtos.add(new AuthorWithFeeDto(
                participant.getUser().getId(),
                participant.getUser().getFio(),
                participant.getUser().getUsername(),
                participant.getFee()
            ));
        }
        return authorWithFeeDtos;
    }

}
