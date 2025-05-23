package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.MessageToUserDto;
import by.forward.forward_system.core.dto.ui.ReviewDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.messager.ws.WebsocketMassageService;
import by.forward.forward_system.core.utils.Constants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReviewService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AttachmentRepository attachmentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final MessageService messageService;
    private final ChatRepository chatRepository;
    private final ChatMessageTypeRepository chatMessageTypeRepository;
    private final BotNotificationService botNotificationService;
    private final WebsocketMassageService websocketMassageService;

    public ReviewEntity saveNewReviewRequest(Long orderId, Long expertId, Long attachmentId, String messageText, AttachmentEntity additionalFile) {
        ReviewEntity reviewEntity = new ReviewEntity();

        reviewEntity.setOrder(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
        reviewEntity.setAttachment(attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Attachment not found")));
        reviewEntity.setReviewedBy(userRepository.findById(expertId).orElseThrow(() -> new RuntimeException("Expert not found")));
        reviewEntity.setAdditionalAttachment(additionalFile);

        reviewEntity.setIsReviewed(false);
        reviewEntity.setIsAccepted(false);
        reviewEntity.setCreatedAt(LocalDateTime.now());

        reviewEntity.setReviewMessage(messageText);

        ReviewEntity save = reviewRepository.save(reviewEntity);

        botNotificationService.sendBotNotification(expertId, "У вас новый запрос на проверку работы!");

        return save;
    }

    public ReviewEntity updateReviewRequest(Long orderId, Long expertId, Long reviewId, Long attachmentId, String messageText, AttachmentEntity additionalFile) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));

        if (additionalFile != null) {
            reviewEntity.setAdditionalAttachment(additionalFile);
        }

        reviewEntity.setOrder(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
        reviewEntity.setAttachment(attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Attachment not found")));
        reviewEntity.setReviewedBy(userRepository.findById(expertId).orElseThrow(() -> new RuntimeException("Expert not found")));
        reviewEntity.setReviewMessage(messageText);

        ReviewEntity save = reviewRepository.save(reviewEntity);

        botNotificationService.sendBotNotification(expertId, "У вас новый запрос на проверку работы!");

        return save;
    }

    public ReviewEntity toEntity(ReviewDto reviewDto) {
        UserEntity userEntity = userRepository.findById(reviewDto.getReviewedByUserId()).orElseThrow(() -> new RuntimeException("user not found"));
        OrderEntity orderEntity = orderRepository.findById(reviewDto.getOrderId()).orElseThrow(() -> new RuntimeException("order not found"));
        AttachmentEntity attachmentEntity = attachmentRepository.findById(reviewDto.getAttachmentId()).orElseThrow(() -> new RuntimeException("attachment not found"));

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(reviewDto.getId());
        reviewEntity.setOrder(orderEntity);
        reviewEntity.setAttachment(attachmentEntity);
        reviewEntity.setReviewMessage(reviewDto.getReviewMessage());
        reviewEntity.setReviewVerdict(reviewDto.getReviewVerdict());
        reviewEntity.setIsReviewed(reviewDto.getIsReviewed());
        reviewEntity.setIsAccepted(reviewDto.getIsAccepted());
        reviewEntity.setReviewedBy(userEntity);
        reviewEntity.setReviewDate(reviewDto.getReviewDate());
        reviewEntity.setCreatedAt(reviewDto.getCreatedAt());

        return reviewEntity;
    }

    public ReviewDto toDto(ReviewEntity reviewEntity) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setId(reviewEntity.getId());
        reviewDto.setExpertUsername(reviewEntity.getReviewedBy().getUsername());
        reviewDto.setOrderId(reviewEntity.getOrder().getId());
        reviewDto.setAttachmentId(reviewEntity.getAttachment().getId());
        if (reviewEntity.getAdditionalAttachment() != null) {
            reviewDto.setAdditionalAttachmentId(reviewEntity.getAdditionalAttachment().getId());
        }
        reviewDto.setReviewMessage(reviewEntity.getReviewMessage());
        reviewDto.setReviewVerdict(reviewEntity.getReviewVerdict());
        reviewDto.setIsReviewed(reviewEntity.getIsReviewed());
        reviewDto.setIsAccepted(reviewEntity.getIsAccepted());
        reviewDto.setReviewedByUserId(reviewEntity.getReviewedBy().getId());
        reviewDto.setReviewDate(reviewEntity.getReviewDate());
        reviewDto.setCreatedAt(reviewEntity.getCreatedAt());
        reviewDto.setReviewVerdictMark(reviewEntity.getReviewMark());
        if (reviewEntity.getReviewAttachment() != null) {
            reviewDto.setReviewFileId(reviewEntity.getReviewAttachment().getId());
        }
        return reviewDto;
    }

    public List<ReviewProjectionDto> getNotReviewed(int page) {
        ;
        return reviewRepository.findAllNotReviewed((page - 1) * Constants.REVIEWS_PAGE_SIZE, Constants.REVIEWS_PAGE_SIZE);
    }

    public List<ReviewProjectionDto> getAllReviews(int page) {
        return reviewRepository.findPage((page - 1) * Constants.REVIEWS_PAGE_SIZE, Constants.REVIEWS_PAGE_SIZE);
    }

    public List<ReviewProjectionDto> getAllReviews(String techNumber) {
        List<ReviewEntity> all = reviewRepository.findByTechNumber(techNumber);
        return getReviewProjectionDtos(all);
    }

    private String getStatusRus(ReviewEntity t) {
        if (!t.getIsReviewed())
            return "Не проверено";
        if (t.getIsAccepted()) {
            return "Проверено. Принято";
        } else {
            return "Проверено, но не принято";
        }
    }

    public List<ReviewProjectionDto> getNotReviewedByUser(Long currentUserId) {
        return reviewRepository.findAllByExpertAndReviewStatusIs(currentUserId, false);
    }

    public List<ReviewProjectionDto> getReviewedByUser(Long currentUserId) {
        return reviewRepository.findAllByExpertAndReviewStatusIs(currentUserId, true);
    }

    private List<ReviewProjectionDto> getReviewProjectionDtos(List<ReviewEntity> all) {
        return all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getCreatedAt).reversed())
            .map(t -> {
                ReviewProjectionDto reviewProjectionDto = new ReviewProjectionDto();
                reviewProjectionDto.setReviewId(t.getId());
                reviewProjectionDto.setCreatedAt(t.getCreatedAt());
                reviewProjectionDto.setReviewedAt(t.getReviewDate());
                reviewProjectionDto.setOrderId(t.getOrder().getId());
                reviewProjectionDto.setOrderTechNumber(new BigDecimal(t.getOrder().getTechNumber()));
                reviewProjectionDto.setOrderName(t.getOrder().getName());
                reviewProjectionDto.setIsReviewed(t.getIsReviewed());
                reviewProjectionDto.setReviewStatusName(getStatusRus(t));
                return reviewProjectionDto;
            })
            .toList();
    }

    public ReviewDto getReviewById(Long reviewId) {
        return toDto(reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("review not found")));
    }

    public List<ReviewDto> getOlderReviews(Long orderId) {
        return reviewRepository.findOldReviews(orderId).stream().map(this::toDto).toList();
    }

    @Transactional
    public void saveVerdict(Long reviewId, String verdict, String verdictMark, Long fileId) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        AttachmentEntity attachment = attachmentRepository.findById(fileId).orElseThrow(() -> new RuntimeException("Attachment not found"));

        reviewEntity.setIsReviewed(true);
        reviewEntity.setReviewDate(LocalDateTime.now());
        reviewEntity.setReviewVerdict(verdict);
        reviewEntity.setReviewMark(verdictMark);
        reviewEntity.setReviewAttachment(attachment);

        reviewEntity = reviewRepository.save(reviewEntity);

        if (verdictMark.equals("2")) {
            String techNumber = reviewEntity.getOrder().getTechNumber();
            String expertUsername = reviewEntity.getReviewedBy().getUsername();

            Map<ParticipantType, List<OrderParticipantEntity>> typeToUser = reviewEntity.getOrder().getOrderParticipants().stream()
                .collect(Collectors.groupingBy(op -> op.getParticipantsType().getType()));

            String authorUsername = Optional.ofNullable(typeToUser.get(ParticipantType.MAIN_AUTHOR)).stream()
                .flatMap(Collection::stream)
                .findAny()
                .map(OrderParticipantEntity::getUser)
                .map(UserEntity::getUsername)
                .orElse("Не найден.");

            String hostUsername = Optional.ofNullable(typeToUser.get(ParticipantType.HOST)).stream()
                .flatMap(Collection::stream)
                .findAny()
                .map(OrderParticipantEntity::getUser)
                .map(UserEntity::getUsername)
                .orElse("Не найден.");

            String text = """
                По заказу №%s поставлена оценка "2" от эксперта %s.
                
                Автор, который писал работу -> %s
                Менеджер, который ведёт заказ -> %s
                """.formatted(techNumber, expertUsername, authorUsername, hostUsername);

            botNotificationService.sendBotNotificationToAdmins(text);
        }
    }

    public void notifyThatRequestApproved(Long chatId, Long reviewId) {
        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("chat not found: " + chatId));
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("review not found: " + reviewId));
        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("CMT not found"));

        String message = """
            Эксперт проверил работу и поставил оценку: %s. Внимательно оцените результат, он доступен в карточке проверки, в меню. Так же прикреплёна таблица с результатами проверки.
            """.formatted(reviewEntity.getReviewMark());

        ChatMessageAttachmentEntity attachmentEntity = new ChatMessageAttachmentEntity();
        attachmentEntity.setAttachment(reviewEntity.getReviewAttachment());

        messageService.sendMessage(null, chatEntity, message, true, chatMessageTypeEntity, List.of(attachmentEntity), List.of());
    }

    public void acceptReview(Long orderId, Long reviewId, Boolean verdict, Boolean sendToChat) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        reviewEntity.setIsAccepted(true);
        reviewEntity = reviewRepository.save(reviewEntity);

        if (verdict) {
            orderService.changeStatus(orderId, OrderStatus.GUARANTEE);
            return;
        }

        if (sendToChat) {
            Long orderChatId = orderService.getOrderMainChat(orderId);
            ChatEntity chatEntity = chatRepository.findById(orderChatId).orElseThrow(() -> new RuntimeException("Chat not found"));
            ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName()).orElseThrow(() -> new RuntimeException("CMT not found"));

            ChatMessageAttachmentEntity attachmentEntity = new ChatMessageAttachmentEntity();
            attachmentEntity.setAttachment(reviewEntity.getReviewAttachment());

            ChatMessageEntity chatMessageEntity = messageService.sendMessage(null,
                chatEntity,
                "Получена рецензия от экспертного отдела. Изучите внимательно файл и выполните доработку.",
                true,
                chatMessageTypeEntity,
                Collections.singletonList(attachmentEntity),
                Collections.emptyList()
            );

            MessageDto messageDto = messageService.convertChatMessage(chatMessageEntity);
            List<Long> users = messageDto.getMessageToUser().stream().map(MessageToUserDto::getUserId).toList();

            websocketMassageService.sendMessageToUsers(users, messageDto);
        }

        orderService.changeStatus(orderId, OrderStatus.FINALIZATION);
    }

    public Integer getNotAcceptedCount() {
        return reviewRepository.countAllByIsAcceptedIsFalse();
    }

    public int getReviewsPageCount() {
        int count = (int) reviewRepository.count();
        return count / Constants.REVIEWS_PAGE_SIZE + (count % Constants.REVIEWS_PAGE_SIZE == 0 ? 0 : 1);
    }

    public int getNotReviewedPageCount() {
        int count = (int) reviewRepository.countNotReviewed();
        return count / Constants.REVIEWS_PAGE_SIZE + (count % Constants.REVIEWS_PAGE_SIZE == 0 ? 0 : 1);
    }
}
