package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.ui.*;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.DisciplineRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.AuthorWithDisciplineProjection;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.services.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final BanService banService;

    private AIDetector aiDetector;

    private final static ObjectMapper MAPPER = new ObjectMapper();

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
        List<ParticipantType> participantTypes = Arrays.asList(ParticipantType.CATCHER, ParticipantType.HOST, ParticipantType.MAIN_AUTHOR);

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

    @Transactional
    public OrderUiDto createOrder(OrderUiDto order) {
        OrderEntity entity = toEntity(order);
        UserEntity userEntity = userRepository.findById(userUiService.getCurrentUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        entity.setCreatedBy(userEntity);
        entity = orderService.save(entity);
        return toDto(entity);
    }

    @Transactional
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
        List<AuthorWithDisciplineProjection> allAuthors = authorService.getAllAuthorsWithDiscipline();

        Set<String> usernames = new HashSet<>();
        Map<String, Long> usernameToId = new HashMap<>();
        Map<Long, List<AuthorWithDisciplineProjection>> idToDisciplines = new HashMap<>();
        Map<Long, String> idToFio = new HashMap<>();
        for (AuthorWithDisciplineProjection author : allAuthors) {
            idToDisciplines.putIfAbsent(author.getId(), new ArrayList<>());
            idToDisciplines.get(author.getId()).add(author);
            usernames.add(author.getUsername());
            usernameToId.put(author.getUsername(), author.getId());
        }

        for (AuthorEntity authorEntity : authorService.getAllAuthorsFast()) {
            idToDisciplines.putIfAbsent(authorEntity.getId(), new ArrayList<>());
            usernames.add(authorEntity.getUser().getUsername());
            usernameToId.put(authorEntity.getUser().getUsername(), authorEntity.getId());
            idToFio.put(authorEntity.getId(), authorEntity.getUser().getFio());
        }

        OrderEntity order = orderService.getById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        List<OrderParticipantEntity> orderParticipants = order.getOrderParticipants();
        List<OrderParticipantEntity> authorsParticipants = orderParticipants.stream()
            .filter(this::isAssignedAuthor)
            .toList();

        Set<Long> authorIds = authorsParticipants.stream()
            .map(OrderParticipantEntity::getUser)
            .map(UserEntity::getId)
            .collect(Collectors.toSet());

        return usernames.stream()
            .sorted(Comparator.comparing(t -> calcOrder(idToDisciplines, usernameToId.get((String) t), order)).thenComparing(o -> ((String) o).toLowerCase()))
            .map(t -> new UserSelectionWithGradeDto(usernameToId.get(t), idToFio.get(usernameToId.get(t)), t, authorIds.contains(usernameToId.get(t)), calcBgColor(idToDisciplines, usernameToId.get(t), order)))
            .toList();
    }

    private int calcOrder(Map<Long, List<AuthorWithDisciplineProjection>> idToDisciplines, Long authorId, OrderEntity order) {
        boolean hasExcellent = false;
        boolean hasGood = false;
        boolean hasMaybe = false;

        List<AuthorWithDisciplineProjection> disciplines = idToDisciplines.get(authorId);
        for (AuthorWithDisciplineProjection discipline : disciplines) {
            if (order.getDiscipline().getId().equals(discipline.getDisciplineId())) {
                continue;
            }
            if (discipline.getDisciplineQuality().equals(DisciplineQualityType.EXCELLENT.getName())) {
                hasExcellent = true;
            }
            if (discipline.getDisciplineQuality().equals(DisciplineQualityType.GOOD.getName())) {
                hasGood = true;
            }
            if (discipline.getDisciplineQuality().equals(DisciplineQualityType.MAYBE.getName())) {
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

    private String calcBgColor(Map<Long, List<AuthorWithDisciplineProjection>> idToDisciplines, Long authorId, OrderEntity order) {
        int comp = calcOrder(idToDisciplines, authorId, order);
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
        List<AuthorEntity> allAuthors = authorService.getAllAuthorsFast();

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
            orderEntity.getAmount(),
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
        orderEntity.setAmount(orderUiDto.getAmount());
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
        Long currentUserId = userUiService.getCurrentUserId();
        List<ParticipantType> participantTypes = Arrays.asList(ParticipantType.CATCHER, ParticipantType.HOST, ParticipantType.MAIN_AUTHOR);
        return orderService.getOrdersCount(currentUserId, participantTypes);
    }

    public int countOrderAuthors(Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        return (int) order.getParticipants().stream()
            .filter(t -> t.getParticipantsType().equals(ParticipantType.MAIN_AUTHOR.getName()))
            .count();
    }

    @SneakyThrows
    public boolean checkOrderByAi(OrderUiDto orderInfo) {
        UserShortUiDto currentUser = userUiService.getCurrentUser();

        String orderText = getOrderData(orderInfo);
        AIDetector.AICheckResult checkResult = aiDetector.isValidMessage(orderText, currentUser.getUsername());

        if (!checkResult.isOk()) {
            return !banService.ban(currentUser.getId(), """
                Создание/обновление заказа с указанием данных, котоые не прошли проверку.
                Данные заказа:
                %s
                Лог проверки <a href="/ai-log/%d" target="_blank">Лог проверки</a>
                """.formatted(orderText, checkResult.aiLogId()));
        }

        return true;
    }

    private String getOrderData(OrderUiDto orderUiDto) {
        List<String> data = new ArrayList<>();

        data.add("Название: " + orderUiDto.getName());
        data.add("Номер ТЗ: " + orderUiDto.getTechNumber());
        data.add("Тип работы: " + orderUiDto.getWorkType());
        data.add("Дисциплина: " + orderUiDto.getDiscipline());
        data.add("Предмет: " + orderUiDto.getSubject());
        data.add("Объём работы: " + orderUiDto.getAmount());
        data.add("Оригинальность: " + orderUiDto.getOriginality().toString());
        data.add("Система проверки: " + orderUiDto.getVerificationSystem());
        data.add("Промежуточный срок сдачи: " + dateToString(orderUiDto.getIntermediateDeadline()));
        data.add("Окончательный срок сдачи: " + dateToString(orderUiDto.getDeadline()));
        data.add("Доп. информация: " + orderUiDto.getVerificationSystem());
        data.add("Цена: " + orderUiDto.getAuthorCost().toString());
        data.add("Дополнительные этапы сдачи: " + orderUiDto.getAdditionalDates());

        return String.join("\n", data);
    }

    private String dateToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "не указано";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return localDateTime.format(formatter);
    }
}
