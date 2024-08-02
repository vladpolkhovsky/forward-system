package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.ui.ReviewDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.messager.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {


    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AttachmentRepository attachmentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final MessageService messageService;
    private final ChatService chatService;
    private final ChatRepository chatRepository;
    private final ChatMessageTypeRepository chatMessageTypeRepository;

    public void saveNewReviewRequest(Long orderId, Long attachmentId, String messageText) {
        ReviewEntity reviewEntity = new ReviewEntity();

        reviewEntity.setOrder(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
        reviewEntity.setAttachment(attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Attachment not found")));
        reviewEntity.setReviewedBy(getOrderExpert(orderId));

        reviewEntity.setIsReviewed(false);
        reviewEntity.setIsAccepted(false);
        reviewEntity.setCreatedAt(LocalDateTime.now());

        reviewEntity.setReviewMessage(messageText);

        reviewRepository.save(reviewEntity);
    }

    public void updateReviewRequest(Long orderId, Long reviewId, Long attachmentId, String messageText) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        reviewEntity.setOrder(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
        reviewEntity.setAttachment(attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Attachment not found")));
        reviewEntity.setReviewedBy(getOrderExpert(orderId));
        reviewEntity.setReviewMessage(messageText);
        reviewRepository.save(reviewEntity);
    }

    public UserEntity getOrderExpert(Long orderId) {
        return orderService.findExpert(orderId);
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
        reviewDto.setOrderId(reviewEntity.getOrder().getId());
        reviewDto.setAttachmentId(reviewEntity.getAttachment().getId());
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

    public List<ReviewProjectionDto> getNotReviewed() {
        List<ReviewEntity> all = reviewRepository.findAll();
        List<ReviewEntity> list = all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getId).reversed())
            .filter(t -> !t.getIsReviewed())
            .toList();
        return getReviewProjectionDtos(list);
    }

    public List<ReviewProjectionDto> getAllReviews() {
        List<ReviewEntity> all = reviewRepository.findAll();
        return getReviewProjectionDtos(all);
    }

    private String getStatusRus(ReviewEntity t) {
        if (!t.getIsReviewed())
            return "Не проверено";
        if (t.getIsAccepted()) {
            return "Проврено. Принято";
        } else {
            return "Проверено, но не принято";
        }
    }

    public List<ReviewProjectionDto> getNotReviewedByUser(Long currentUserId) {
        List<ReviewEntity> all = reviewRepository.findAllByUserId(currentUserId);
        return getReviewProjectionDtos(all);
    }

    private List<ReviewProjectionDto> getReviewProjectionDtos(List<ReviewEntity> all) {
        return all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getId).reversed())
            .map(t -> {
                ReviewProjectionDto reviewProjectionDto = new ReviewProjectionDto();
                reviewProjectionDto.setReviewId(t.getId());
                reviewProjectionDto.setOrderId(t.getOrder().getId());
                reviewProjectionDto.setOrderTechNumber(t.getOrder().getTechNumber());
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

    public void saveVerdict(Long reviewId, String verdict, Integer verdictMark, Long fileId) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        AttachmentEntity attachment = attachmentRepository.findById(fileId).orElseThrow(() -> new RuntimeException("Attachment not found"));

        reviewEntity.setIsReviewed(true);
        reviewEntity.setReviewDate(LocalDateTime.now());
        reviewEntity.setReviewVerdict(verdict);
        reviewEntity.setReviewMark(verdictMark);
        reviewEntity.setReviewAttachment(attachment);

        reviewRepository.save(reviewEntity);
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

            messageService.sendMessage(null,
                chatEntity,
                "Эксперт прислал рецензаю для доработки",
                true,
                chatMessageTypeEntity,
                Collections.singletonList(attachmentEntity),
                Collections.emptyList()
            );
        }

        orderService.changeStatus(orderId, OrderStatus.FINALIZATION);
    }
}
