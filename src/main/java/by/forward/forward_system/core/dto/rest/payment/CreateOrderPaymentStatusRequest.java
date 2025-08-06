package by.forward.forward_system.core.dto.rest.payment;

import by.forward.forward_system.core.enums.OrderPaymentStatus;
import lombok.Data;

@Data
public class CreateOrderPaymentStatusRequest {
    private Long userId;
    private Long orderId;
    private OrderPaymentStatus status;
    private Double paymentValue;
}
