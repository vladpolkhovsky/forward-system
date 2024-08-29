package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.*;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.DisciplineRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.services.core.AuthorService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderUiService {

    private final OrderService orderService;

    private final UserService userService;

    private final AuthorService authorService;

    private final UserUiService userUiService;

    private final UserRepository userRepository;
    private final DisciplineRepository disciplineRepository;

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

    public List<OrderUiDto> getAllMyOrders(Long currentUserId) {
        List<ParticipantType> participantTypes = Arrays.asList(ParticipantType.CATCHER, ParticipantType.HOST);

        Predicate<OrderEntity> isParticipant = (order) -> {
            List<OrderParticipantEntity> orderParticipants = order.getOrderParticipants();
            for (OrderParticipantEntity orderParticipant : orderParticipants) {
                if (participantTypes.contains(orderParticipant.getParticipantsType().getType())
                    && orderParticipant.getUser().getId().equals(currentUserId)) {
                    return true;
                }
            }
            return false;
        };

        return orderService.findAllOrder().stream()
            .filter(isParticipant)
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
        UserEntity userEntity = userRepository.findById(userUiService.getCurrentUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        entity.setCreatedBy(userEntity);
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
        return isDistributionStatus(orderEntity);
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

    public List<UserSelectionWithGradeDto> getAuthorListWithAuthorMark(Long orderId) {
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
            .sorted(Comparator.comparing(t -> calcOrder((AuthorEntity) t, order)).thenComparing(o -> ((AuthorEntity)o).getUser().getUsername()))
            .map(t -> new UserSelectionWithGradeDto(t.getId(), t.getUser().getFio(), t.getUser().getUsername(), authorIds.contains(t.getUser().getId()), calcBgColor(order, t)))
            .toList();
    }

    private int calcOrder(AuthorEntity authorEntity, OrderEntity order) {
        boolean hasExcellent = false;
        boolean hasGood = false;
        boolean hasMaybe = false;

        for (AuthorDisciplineEntity authorDiscipline : authorEntity.getAuthorDisciplines()) {
            if (!order.getDiscipline().getId().equals(authorDiscipline.getDiscipline().getId())) {
                continue;
            }
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.EXCELLENT)) {
                hasExcellent = true;
            }
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.GOOD)) {
                hasGood = true;
            }
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.MAYBE)) {
                hasMaybe = true;
            }
        }

        if (hasExcellent) {
            return 0;
        }
        if (hasGood) {
            return 1;
        }
        if (hasMaybe) {
            return 2;
        }
        return 3;
    }

    private String calcBgColor(OrderEntity order, AuthorEntity t) {
        int comp = calcOrder(t, order);
        if (comp == 0) {
            return "text-bg-success";
        }
        if (comp == 1) {
            return "text-bg-warning";
        }
        if (comp == 2) {
            return "text-bg-primary";
        } else {
            return "text-bg-light";
        }
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
            new BigDecimal(orderEntity.getTechNumber()),
            orderEntity.getName(),
            orderEntity.getWorkType(),
            orderEntity.getDiscipline().getName(),
            orderEntity.getDiscipline().getId(),
            orderEntity.getSubject(),
            orderEntity.getOriginality(),
            orderEntity.getOrderStatus().getStatus().getName(),
            orderEntity.getOrderStatus().getStatus().getRusName(),
            orderEntity.getVerificationSystem(),
            orderEntity.getAdditionalDates(),
            orderEntity.getIntermediateDeadline(),
            orderEntity.getDeadline(),
            orderEntity.getOther(),
            orderEntity.getTakingCost(),
            orderEntity.getAuthorCost(),
            orderEntity.getCreatedAt(),
            findResponsibleManager(orderEntity),
            calcDistributionDays(orderEntity),
            isDistributionStatus(orderEntity)
        );
    }

    private Boolean isDistributionStatus(OrderEntity orderEntity) {
        List<OrderStatus> catcherStatus = Arrays.asList(OrderStatus.DISTRIBUTION, OrderStatus.CREATED);
        return catcherStatus.contains(orderEntity.getOrderStatus().getStatus());
    }

    private Integer calcDistributionDays(OrderEntity orderEntity) {
        if (!isDistributionStatus(orderEntity)) {
            return null;
        }
        return Math.toIntExact(
            Duration.between(orderEntity.getCreatedAt(), LocalDateTime.now()).toDays()
        );
    }

    private String findResponsibleManager(OrderEntity orderEntity) {
        ParticipantType participantType = ParticipantType.CATCHER;
        List<OrderStatus> catcherStatus = Arrays.asList(OrderStatus.DISTRIBUTION, OrderStatus.CREATED);

        if (!catcherStatus.contains(orderEntity.getOrderStatus().getStatus())) {
            participantType = ParticipantType.HOST;
        }

        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (orderParticipant.getParticipantsType().getType().equals(participantType)) {
                return orderParticipant.getUser().getUsername();
            }
        }
        return "Не назначен";
    }

    private OrderEntity toEntity(OrderUiDto orderUiDto) {
        DisciplineEntity discipline = disciplineRepository.findById(orderUiDto.getDisciplineId())
            .orElseThrow(() -> new RuntimeException("Discipline not found"));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderUiDto.getId());
        orderEntity.setName(orderUiDto.getName());
        orderEntity.setTechNumber(orderUiDto.getTechNumber().toString());
        orderEntity.setWorkType(orderUiDto.getWorkType());
        orderEntity.setDiscipline(discipline);
        orderEntity.setSubject(orderUiDto.getSubject());
        orderEntity.setOriginality(orderUiDto.getOriginality());
        orderEntity.setVerificationSystem(orderUiDto.getVerificationSystem());
        orderEntity.setAdditionalDates(orderUiDto.getAdditionalDates());
        orderEntity.setIntermediateDeadline(orderUiDto.getIntermediateDeadline());
        orderEntity.setDeadline(orderUiDto.getDeadline());
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

    public BigDecimal getLastTechNumber() {
        return new BigDecimal(orderService.getLastTechNumber());
    }

    public Integer countMyOrders() {
        return orderService.countMyOrders(userUiService.getCurrentUserId());
    }
}
