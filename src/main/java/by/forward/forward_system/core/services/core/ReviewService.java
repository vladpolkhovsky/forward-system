package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.ui.ReviewDto;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AttachmentRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.ReviewRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public void saveNewReviewRequest(Long orderId, Long attachmentId, String messageText) {
        ReviewEntity reviewEntity = new ReviewEntity();

        reviewEntity.setOrder(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
        reviewEntity.setAttachment(attachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Attachment not found")));
        reviewEntity.setReviewedBy(getOrderExpert(orderId));

        reviewEntity.setIsReviewed(false);
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
        reviewDto.setReviewedByUserId(reviewEntity.getReviewedBy().getId());
        reviewDto.setReviewDate(reviewEntity.getReviewDate());
        reviewDto.setCreatedAt(reviewEntity.getCreatedAt());
        return reviewDto;
    }

    public List<ReviewProjectionDto> getNotReviewed() {
        List<ReviewEntity> all = reviewRepository.findAll();
        return all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getId).reversed())
            .filter(t -> !t.getIsReviewed())
            .map(t -> {
                ReviewProjectionDto reviewProjectionDto = new ReviewProjectionDto();
                reviewProjectionDto.setReviewId(t.getId());
                reviewProjectionDto.setOrderId(t.getOrder().getId());
                reviewProjectionDto.setOrderTechNumber(t.getOrder().getTechNumber());
                reviewProjectionDto.setOrderName(t.getOrder().getName());
                reviewProjectionDto.setIsReviewed(false);
                reviewProjectionDto.setReviewStatusName("Не проверено");
                return reviewProjectionDto;
            })
            .toList();
    }

    public List<ReviewProjectionDto> getAllReviews() {
        List<ReviewEntity> all = reviewRepository.findAll();
        return all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getId).reversed())
            .map(t -> {
                ReviewProjectionDto reviewProjectionDto = new ReviewProjectionDto();
                reviewProjectionDto.setReviewId(t.getId());
                reviewProjectionDto.setOrderId(t.getOrder().getId());
                reviewProjectionDto.setOrderTechNumber(t.getOrder().getTechNumber());
                reviewProjectionDto.setOrderName(t.getOrder().getName());
                reviewProjectionDto.setIsReviewed(t.getIsReviewed());
                reviewProjectionDto.setReviewStatusName(t.getIsReviewed() ? "Проверено" : "Не проверено");
                return reviewProjectionDto;
            })
            .toList();
    }

    public List<ReviewProjectionDto> getNotReviewedByUser(Long currentUserId) {
        List<ReviewEntity> all = reviewRepository.findAllByUserId(currentUserId);
        return all.stream()
            .sorted(Comparator.comparing(ReviewEntity::getId).reversed())
            .map(t -> {
                ReviewProjectionDto reviewProjectionDto = new ReviewProjectionDto();
                reviewProjectionDto.setReviewId(t.getId());
                reviewProjectionDto.setOrderId(t.getOrder().getId());
                reviewProjectionDto.setOrderTechNumber(t.getOrder().getTechNumber());
                reviewProjectionDto.setOrderName(t.getOrder().getName());
                reviewProjectionDto.setIsReviewed(t.getIsReviewed());
                reviewProjectionDto.setReviewStatusName(t.getIsReviewed() ? "Проверено" : "Не проверено");
                return reviewProjectionDto;
            })
            .toList();
    }

    public ReviewDto getReviewById(Long reviewId) {
        return toDto(reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("review not found")));
    }

    public void saveVerdict(Long reviewId, String verdict) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));

        reviewEntity.setIsReviewed(true);
        reviewEntity.setReviewDate(LocalDateTime.now());
        reviewEntity.setReviewVerdict(verdict);

        reviewEntity = reviewRepository.save(reviewEntity);
    }
}
