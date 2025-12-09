package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.messenger.v3.CreateDistributionRequestDto;
import by.forward.forward_system.core.dto.messenger.v3.DistributionLogDto;
import by.forward.forward_system.core.dto.messenger.v3.DistributionPersonDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorFullDto;
import by.forward.forward_system.core.enums.DistributionItemStatusType;
import by.forward.forward_system.core.enums.DistributionStatusType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.OrderParticipantRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository.OrderSendRequestProjection;
import by.forward.forward_system.core.jpa.repository.QueueDistributionRepository;
import by.forward.forward_system.core.mapper.QueueDistributionMapper;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NewDistributionService {

    private final OrderParticipantRepository participantRepository;
    private final OrderService orderService;
    private final QueueDistributionRepository queueDistributionRepository;
    private final QueueDistributionMapper queueDistributionMapper;
    private final BotNotificationService botNotificationService;
    private final MessageService messageService;
    private final OrderRepository orderRepository;


    @Transactional
    public void stopDistribution(Long distributionId) {
        queueDistributionRepository.findById(distributionId).ifPresent(distribution -> {
            distribution.setStatus(DistributionStatusType.ENDED_BY_MANAGER);

            distribution.getItems().stream()
                .filter(item -> item.getStatus() == DistributionItemStatusType.IN_PROGRESS
                    || item.getStatus() == DistributionItemStatusType.TALK)
                .forEach(item -> {
                    item.setStatus(DistributionItemStatusType.SKIPPED);
                    messageService.sendMessageToNewOrderChat(item.getUser().getId(),
                        item.getQueueDistribution().getCreatedBy().getId(),
                        true,
                        "Распределение заказа отменено.",
                        Map.of(),
                        false
                    );
                });

            distribution.getItems().stream()
                .filter((item -> item.getStatus() == DistributionItemStatusType.WAITING))
                .forEach(item -> item.setStatus(DistributionItemStatusType.SKIPPED));

            getUsersThatShouldBeNotified(distribution).forEach(user -> botNotificationService.sendBotNotification(
                user.getId(), """
                    Автоматическое распредление по заказу №%s принудительно завершено.
                    """.formatted(distribution.getOrder().getTechNumber())));

        });
    }

    @Transactional
    public void deleteAuthorParticipant(Long orderId, Long authorId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Нет заказа с id = " + orderId));

        if (orderEntity.getOrderStatus().getStatus() != OrderStatus.DISTRIBUTION
            && orderEntity.getOrderStatus().getStatus() != OrderStatus.CREATED) {
            throw new IllegalArgumentException("Заказ не в статусе \"Создан; Распределение\". Невозможно удалить участника заказа.");
        }

        boolean inInProgressDistribution = queueDistributionRepository.isUserInInProgressDistribution(orderId, authorId);
        if (inInProgressDistribution) {
            throw new IllegalArgumentException("Автор есть в активном автоматическом расспределении.");
        }

        orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType() == ParticipantType.AUTHOR || t.getParticipantsType().getType() == ParticipantType.DECLINE_AUTHOR)
            .filter(t -> Objects.equals(t.getUser().getId(), authorId))
            .forEach(participantRepository::delete);
    }

    @Transactional
    public void updateManagerRole(Long orderId, Long userId, ParticipantType participantType) {
        if (!List.of(ParticipantType.HOST, ParticipantType.CATCHER, ParticipantType.EXPERT).contains(participantType)) {
            throw new IllegalArgumentException("Can't update role " + participantType);
        }

        List<OrderParticipantEntity> participants = participantRepository.getOrderParticipantsByOrderId(orderId);
        List<OrderParticipantEntity> toRemove = participants.stream().filter(t -> t.getParticipantsType().getType() == participantType)
            .toList();

        participantRepository.deleteAll(toRemove);

        OrderParticipantEntity newParticipant = new OrderParticipantEntity();
        newParticipant.setParticipantsType(participantType.toEntity());
        newParticipant.setOrder(OrderEntity.of(orderId));
        newParticipant.setUser(UserEntity.of(userId));

        participantRepository.save(newParticipant);
    }

    @Transactional
    public void createDistribution(Long orderId, CreateDistributionRequestDto createDistributionRequestDto) {
        if (CollectionUtils.isEmpty(createDistributionRequestDto.getPersons())) {
            throw new IllegalArgumentException("empty persons collection");
        }

        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow((() -> new IllegalArgumentException("not found order")));

        List<DistributionPersonDto> persons = createDistributionRequestDto.getPersons();
        List<OrderParticipantEntity> participants = createAuthorParticipants(order, persons);

        if (BooleanUtils.isTrue(createDistributionRequestDto.getIsQueueDistribution())) {
            createQueueDistribution(createDistributionRequestDto, order, AuthUtils.getCurrentUserId());
        } else {
            sendRequestToAuthors(participants, AuthUtils.getCurrentUserId(), order, createDistributionRequestDto.getText());
        }

        orderRepository.updateOrderStatus(orderId, OrderStatus.DISTRIBUTION.getName());
    }

    @Transactional(readOnly = true)
    public List<DistributionLogDto> listLogs(Long orderId) {
        List<QueueDistributionEntity> byOrderId = queueDistributionRepository.findByOrderId(orderId);
        return queueDistributionMapper.mapMany(byOrderId);
    }

    private List<OrderParticipantEntity> createAuthorParticipants(OrderEntity order, List<DistributionPersonDto> persons) {
        Integer defaultAuthorsCost = order.getAuthorCost();

        List<OrderParticipantEntity> list = persons.stream().map(p -> {
            OrderParticipantEntity entity = new OrderParticipantEntity();
            entity.setUser(UserEntity.of(p.getUserId()));
            entity.setOrder(order);
            entity.setFee(Optional.ofNullable(p.getCustomFee()).orElse(defaultAuthorsCost));
            entity.setParticipantsType(ParticipantType.AUTHOR.toEntity());
            return entity;
        }).toList();

        return participantRepository.saveAll(list);
    }

    @Transactional
    public void skip(QueueDistributionItemEntity item) {
        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s завершилось. Автор пропущен.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));

        item.setStatus(DistributionItemStatusType.SKIPPED);

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            "Превышено время ожидания ответа. Заказ распределён.",
            Map.of(),
            false
        );
    }

    @Transactional
    public void decline(QueueDistributionItemEntity item) {
        if (item.getStatus() != DistributionItemStatusType.IN_PROGRESS && item.getStatus() != DistributionItemStatusType.TALK) {
            throw new IllegalArgumentException("Статус распределения \"%s\". Невозможно отказаться от заказа."
                .formatted(item.getStatus().getRusName()));
        }

        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s завершилось. Автор отказался от заказа.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));

        item.setStatus(DistributionItemStatusType.DECLINE);

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            "Вы отказались от заказа.",
            Map.of(),
            false
        );
    }

    @Transactional
    public void talk(QueueDistributionItemEntity item) {
        if (item.getStatus() != DistributionItemStatusType.IN_PROGRESS) {
            throw new IllegalArgumentException("Статус распределения \"%s\". Обсуждение заказа недоступно."
                .formatted(item.getStatus().getRusName()));
        }

        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s. Автор решил обсудить заказ.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));

        item.setStatus(DistributionItemStatusType.TALK);

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            "Вы хотите обсудить заказ. Ожидайте ответа менеджера.",
            Map.of(),
            false
        );
    }

    @Transactional
    public void accept(QueueDistributionItemEntity item) {
        if (item.getStatus() != DistributionItemStatusType.IN_PROGRESS) {
            throw new IllegalArgumentException("Статус распределения \"%s\". Невозможно взять заказ в работу."
                .formatted(item.getStatus().getRusName()));
        }

        item.getQueueDistribution().getItems().stream()
            .filter(cItem -> cItem.getStatus() == DistributionItemStatusType.WAITING)
            .forEach(cItem -> cItem.setStatus(DistributionItemStatusType.SKIPPED));

        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s. Автор принял заказ.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));
        item.setStatus(DistributionItemStatusType.ACCEPTED);

        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распределение заказа №%s завершилось. Найден автор, который приянял запрос.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber())));

        getUsersThatCanTalkAboutOrder(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Заказ №%s уже распределен другому автору. Ожидайте новых заказов!
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber())));

        item.getQueueDistribution().setStatus(DistributionStatusType.ENDED);

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            "Вы прияняли заказ. Ожидайте ответа менеджера.",
            Map.of(),
            false
        );
    }

    @Transactional
    public void skipBecauseWaitTooLong(QueueDistributionItemEntity item) {
        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s завершилось без ответа. Обсуждение с автором длится слишком долго.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));

        item.setStatus(DistributionItemStatusType.SKIPPED);
    }

    @Transactional
    public void noResponseOnItem(QueueDistributionItemEntity item) {
        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s завершилось без ответа. Переход к следующему автору.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername())));

        item.setStatus(DistributionItemStatusType.NO_RESPONSE);

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            "Превышено время ожидания ответа. Заказ предложен следующему автору, но ещё актуален. Если вы готовы взять заказ, сообщите об этом в чат.",
            Map.of()
        );
    }

    @Transactional
    public void finishDistributionWithoutResult(QueueDistributionEntity distribution) {
        getUsersThatShouldBeNotified(distribution).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распределение заказа №%s завершилось. Автор не найден.
                """.formatted(distribution.getOrder().getTechNumber())));
    }

    @Transactional
    public void triggerNewItemToStart(QueueDistributionItemEntity item) {
        getUsersThatShouldBeNotified(item.getQueueDistribution()).forEach(user -> botNotificationService.sendBotNotification(
            user.getId(), """
                Автоматическое распредление по заказу №%s для автора %s началось. У автора есть %d минут на ответ.
                """.formatted(item.getQueueDistribution().getOrder().getTechNumber(), item.getUser().getUsername(), item.getQueueDistribution().getQueueDistributionWaitMinutes())));

        item.setStatus(DistributionItemStatusType.IN_PROGRESS);
        item.setWaitStart(LocalDateTime.now());
        item.setWaitUntil(LocalDateTime.now().plusMinutes(item.getQueueDistribution().getQueueDistributionWaitMinutes()));

        Map<String, String> options = Map.of(
            "Ознакомиться с ТЗ/требованиями", "/request-order/" + item.getQueueDistribution().getOrder().getId(),
            "Обсудить заказ", "/api/order/distribution/item?itemId=" + item.getId() + "&action=TALK",
            "Принять заказ", "/api/order/distribution/item?itemId=" + item.getId() + "&action=ACCEPTED",
            "Отказаться от заказа", "/api/order/distribution/item?itemId=" + item.getId() + "&action=DECLINE"
        );

        messageService.sendMessageToNewOrderChat(item.getUser().getId(),
            item.getQueueDistribution().getCreatedBy().getId(),
            true,
            getFormattedMessageForQueueDistribution(item.getQueueDistribution().getOrder().getId()),
            options
        );

        if (StringUtils.isNotBlank(item.getQueueDistribution().getCreatedByUserMessage())) {
            messageService.sendMessageToNewOrderChat(item.getUser().getId(),
                item.getQueueDistribution().getCreatedBy().getId(),
                false,
                item.getQueueDistribution().getCreatedByUserMessage(),
                Map.of()
            );
        }
    }

    private void createQueueDistribution(CreateDistributionRequestDto createDistributionRequestDto, OrderEntity order, Long managerId) {
        List<Long> authorIds = createDistributionRequestDto.getPersons().stream()
            .sorted(Comparator.comparing(DistributionPersonDto::getOrder))
            .map(DistributionPersonDto::getAuthor)
            .map(AuthorFullDto::getId)
            .toList();

        QueueDistributionEntity queueDistributionEntity = new QueueDistributionEntity();

        queueDistributionEntity.setCreatedBy(UserEntity.of(managerId));
        queueDistributionEntity.setOrder(order);
        queueDistributionEntity.setQueueDistributionWaitMinutes(createDistributionRequestDto.getQueueDistributionWaitMinutes());
        queueDistributionEntity.setStatus(DistributionStatusType.PLANNED);
        queueDistributionEntity.setCreatedByUserMessage(StringUtils.trimToNull(createDistributionRequestDto.getText()));
        queueDistributionEntity.setCreatedAt(LocalDateTime.now());
        queueDistributionEntity.setStartTimeAt(Optional.ofNullable(createDistributionRequestDto.getStartTimeAt()).orElse(LocalDateTime.now()));

        for (int i = 0; i < authorIds.size(); i++) {
            Long authorId = authorIds.get(i);

            QueueDistributionItemEntity queueDistributionItemEntity = new QueueDistributionItemEntity();
            queueDistributionItemEntity.setDistributionOrder(i + 1L);
            queueDistributionItemEntity.setUser(UserEntity.of(authorId));
            queueDistributionItemEntity.setStatus(DistributionItemStatusType.WAITING);
            queueDistributionItemEntity.setCreatedAt(LocalDateTime.now());
            queueDistributionItemEntity.setQueueDistribution(queueDistributionEntity);

            queueDistributionEntity.getItems().add(queueDistributionItemEntity);
        }

        queueDistributionRepository.save(queueDistributionEntity);
    }

    private String getFormattedMessageForQueueDistribution(Long orderId) {
        OrderSendRequestProjection orderProjection = orderRepository.findOrderProjectionToOrderSendRequest(orderId);
        return """
            Поступил новый заказ №%s.
            Тип работы "%s".
            Дисциплина "%s".
            Тема "%s".
            У вас ограниченное время для ответа. Нажмите на вариант взаимодействия с заказом ниже.
            """.formatted(orderProjection.getTechNumber(),
            orderProjection.getWorkType(), orderProjection.getDiscipline(), orderProjection.getSubject());
    }

    private void sendRequestToAuthors(List<OrderParticipantEntity> participants, Long managerId, OrderEntity order, String message) {
        for (OrderParticipantEntity participant : participants) {
            orderService.sendNewOrderRequest(participant.getUser().getId(), managerId, order.getId(), message);
        }
    }

    private static final List<DistributionItemStatusType> SHOULD_BE_NOTIFIED_WHEN_AUTHOR_FOUND =
        List.of(DistributionItemStatusType.TALK, DistributionItemStatusType.NO_RESPONSE);

    private static List<UserEntity> getUsersThatCanTalkAboutOrder(QueueDistributionEntity distribution) {
        return distribution.getItems().stream()
            .filter(item -> SHOULD_BE_NOTIFIED_WHEN_AUTHOR_FOUND.contains(item.getStatus()))
            .map(QueueDistributionItemEntity::getUser)
            .toList();
    }

    private static List<UserEntity> getUsersThatShouldBeNotified(QueueDistributionEntity distribution) {
        List<OrderParticipantEntity> participants = distribution.getOrder().getOrderParticipants();

        Stream<UserEntity> catchers = participants.stream()
            .filter(p -> p.getParticipantsType().getType() == ParticipantType.CATCHER)
            .map(OrderParticipantEntity::getUser);

        return Stream.concat(Stream.of(distribution.getOrder().getCreatedBy()), catchers)
            .distinct()
            .toList();
    }

    @Transactional
    public void toInProgress(QueueDistributionEntity distribution) {
        distribution.setStatus(DistributionStatusType.IN_PROGRESS);
    }
}
