package by.forward.forward_system.core.dto.rest.authors;

import by.forward.forward_system.core.enums.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.util.List;

@Data
public class AuthorOrderDto {
    private long orderId;
    private String orderTechNumber;
    private String subject;
    private int originality;
    private List<AuthorAdditionalDatesDto> additionalDates;
    private String deadline;
    private String intermediateDeadline;
    private String orderStatus;
    private String orderStatusRus;
    private OrderPaymentStatus paymentStatus;

    @Data
    @AllArgsConstructor
    public static class AuthorAdditionalDatesDto {
        private String text;
        @With
        private String time;
    }
}
