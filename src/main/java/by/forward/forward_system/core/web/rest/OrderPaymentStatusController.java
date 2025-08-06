package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.payment.CreateOrderPaymentStatusRequest;
import by.forward.forward_system.core.dto.rest.payment.OrderPaymentStatusDto;
import by.forward.forward_system.core.services.NewOrderPaymentService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/order/payment")
@AllArgsConstructor
public class OrderPaymentStatusController {

    private final NewOrderPaymentService newOrderPaymentService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<OrderPaymentStatusDto>> getPaymentStatus() {
        var payments = newOrderPaymentService.findPayments(AuthUtils.getCurrentUserId());
        return ResponseEntity.ok(payments);
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<List<OrderPaymentStatusDto>> getPaymentStatusById(@PathVariable Long userId) {
        var payments = newOrderPaymentService.findPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping(value = "/{userId}/last")
    public ResponseEntity<List<OrderPaymentStatusDto>> getLastPaymentStatusById(@PathVariable Long userId) {
        var payments = newOrderPaymentService.findLastPaymentsStatus(userId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<OrderPaymentStatusDto> createPayment(@RequestBody CreateOrderPaymentStatusRequest request) {
        return ResponseEntity.ok(newOrderPaymentService.save(List.of(request)).getFirst());
    }

    @PostMapping(value = "/create/many")
    public ResponseEntity<List<OrderPaymentStatusDto>> createPayment(@RequestBody List<CreateOrderPaymentStatusRequest> request) {
        var saved = newOrderPaymentService.save(request);
        messageService.sendPaymentStatusNotification(saved);
        return ResponseEntity.ok(saved);
    }
}
