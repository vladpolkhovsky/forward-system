package by.forward.forward_system.core.dto.rest.authors;

import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import by.forward.forward_system.core.enums.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorOrderDto {
    private long orderId;
    private String orderTechNumber;
    private String subject;
    private int originality;
    private List<AdditionalDateDto> additionalDates;
    private String deadline;
    private String intermediateDeadline;
    private String orderStatus;
    private String orderStatusRus;
    private OrderPaymentStatus paymentStatus;
}
