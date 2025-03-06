package by.forward.forward_system.core.jpa.repository.projections;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewProjectionDto {
    private BigDecimal orderTechNumber;
    private Long orderId;
    private Long reviewId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String orderName;
    private Boolean isReviewed;
    private Boolean isAccepted;
    private String reviewStatusName;

    public ReviewProjectionDto(String orderTechNumber,
                               Long orderId,
                               Long reviewId,
                               LocalDateTime createdAt,
                               LocalDateTime reviewedAt,
                               String orderName,
                               Boolean isReviewed,
                               Boolean isAccepted) {
        this.orderTechNumber = new BigDecimal(orderTechNumber);
        this.orderId = orderId;
        this.reviewId = reviewId;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.orderName = orderName;
        this.isReviewed = isReviewed;
        this.reviewStatusName = getStatusRus(isReviewed, isAccepted);
    }

    private static String getStatusRus(Boolean isReviewed,
                                Boolean isAccepted) {
        if (!isReviewed)
            return "Не проверено";
        if (isAccepted) {
            return "Проверено. Принято";
        } else {
            return "Проверено, но не принято";
        }
    }
}
