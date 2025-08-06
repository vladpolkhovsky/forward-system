package by.forward.forward_system.core.dto.rest.payment;

import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.enums.OrderPaymentStatus;
import lombok.Data;

@Data
public class OrderPaymentStatusDto {
    private Long id;
    private AuthorDto user;
    private AuthorOrderDto order;
    private OrderPaymentStatus status;
    private Double paymentValue;
    private String createdAt;
}
