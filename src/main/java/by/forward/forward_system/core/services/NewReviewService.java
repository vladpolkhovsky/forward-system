package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderReviewDto;
import by.forward.forward_system.core.dto.messenger.v3.V3ReviewCreateRequest;
import by.forward.forward_system.core.dto.messenger.v3.V3SearchOrderReviewDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupParticipantStatusDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.mapper.ReviewMapper;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.AuthUtils;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewReviewService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final AttachmentRepository attachmentRepository;
    private final NewCalendarService newCalendarService;
    private final MessageService messageService;
    private final ChatRepository chatRepository;
    private final OrderParticipantRepository orderParticipantRepository;
    private final BotNotificationService botNotificationService;

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

    @Transactional
    public void createAutomaticReviewRequest(V3ReviewCreateRequest createRequest) {
        ChatEntity chat = chatRepository.findById(createRequest.getChatId())
            .orElseThrow(() -> new IllegalArgumentException("chat with id " + createRequest.getChatId() + " not found"));
        OrderEntity order = orderRepository.findById(createRequest.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("order with id " + createRequest.getOrderId() + " not found"));
        AttachmentEntity attachment = attachmentRepository.findById(createRequest.getFileId())
            .orElseThrow(() -> new IllegalArgumentException("file with id " + createRequest.getFileId() + " not found"));

        Long expertCalendarGroupId = Optional.ofNullable(order.getExpertCalendarGroup())
            .map(CalendarGroupEntity::getId)
            .orElseThrow(() -> new IllegalArgumentException("expert calendar group not set"));

        LocalDate currentDay = LocalDate.now();
        CalendarGroupParticipantStatusDto calendarGroupStatus = newCalendarService.getGroupParticipantsStatus(expertCalendarGroupId,
            currentDay.minusDays(1), currentDay.plusDays(1));

        Optional<UserEntity> currentAssignedExpert = order.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType() == ParticipantType.EXPERT)
            .map(OrderParticipantEntity::getUser)
            .findAny();

        String dayKey = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(currentDay);
        UserEntity expert = currentAssignedExpert.or(() -> calendarGroupStatus.getDays()
                .getOrDefault(dayKey, List.of()).stream().findAny().map(UserEntity::of))
            .orElseThrow(() -> new IllegalArgumentException("no active expert found for group id %s and date %s"
                .formatted(expertCalendarGroupId, dayKey)));

        String userComment = StringUtils.defaultString(StringUtils.trimToNull(createRequest.getRequestText()),
            "<Текст не указан>");
        String username = AuthUtils.getCurrentUserDetails().getUsername();

        String text = """
            Пользователь %s создал запрос на проверку и указал следующее сообщение для эксперта:
            ===
            %s
            """.formatted(username, userComment);

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setOrder(order);
        reviewEntity.setAttachment(attachment);
        reviewEntity.setReviewMessage(text);
        reviewEntity.setReviewedBy(expert);
        reviewEntity.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(reviewEntity);

        if (currentAssignedExpert.isEmpty()) {
            OrderParticipantEntity expertParticipant = new OrderParticipantEntity();
            expertParticipant.setUser(expert);
            expertParticipant.setOrder(order);
            expertParticipant.setParticipantsType(ParticipantType.EXPERT.toEntity());

            expertParticipant = orderParticipantRepository.save(expertParticipant);

            order.getOrderParticipants().add(expertParticipant);
        }

        messageService.sendMessage(ChatNames.SYSTEM_USER_ID, true, chat.getId(), """
            Пользователь %s создал запрос на проверку файла.
            Файл для проверки прикреплён к данному сообщению.
            """.formatted(username), List.of(createRequest.getFileId()));

        order.setOrderStatus(OrderStatus.REVIEW.toEntity());

        botNotificationService.sendBotNotification(expert.getId(),
            "У вас новый запрос на проверку работы!");
    }

    @Transactional
    public void deleteReview(Long reviewId, Long chatId) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("review with id " + reviewId + " not found"));

        if (reviewEntity.getIsReviewed()) {
            throw new IllegalArgumentException("Данный запрос уже проверен. Нельзя удалить.");
        }

        reviewRepository.delete(reviewEntity);

        String username = AuthUtils.getCurrentUserDetails().getUsername();
        String message = """
            Пользователь %s удалил запрос на проверку.
            """.formatted(username);

        messageService.sendMessage(ChatNames.SYSTEM_USER_ID, true, chatId, message, List.of());
    }
}
