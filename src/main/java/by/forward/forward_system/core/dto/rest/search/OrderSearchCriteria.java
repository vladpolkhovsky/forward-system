package by.forward.forward_system.core.dto.rest.search;

import by.forward.forward_system.core.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchCriteria {
    private String techNumber;
    private Boolean showClosed;
    private String status;
}
