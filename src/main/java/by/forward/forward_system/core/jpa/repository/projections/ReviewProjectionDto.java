package by.forward.forward_system.core.jpa.repository.projections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewProjectionDto {
    private Integer orderTechNumber;
    private Long orderId;
    private Long reviewId;
    private String orderName;
    private Boolean isReviewed;
    private String reviewStatusName;
}
