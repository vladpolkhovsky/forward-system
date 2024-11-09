package by.forward.forward_system.core.jpa.repository.projections;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewProjectionDto {
    private BigDecimal orderTechNumber;
    private Long orderId;
    private Long reviewId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String orderName;
    private Boolean isReviewed;
    private String reviewStatusName;
}
