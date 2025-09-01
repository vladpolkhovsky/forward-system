package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3SearchOrderReviewDto {
    private Long orderId;
    private String orderTechNumber;
    private OrderStatus status;
    private String statusRusName;
    private List<V3OrderReviewDto> reviews;
}
