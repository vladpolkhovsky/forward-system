package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.admin.OrderRequestLogDto;
import by.forward.forward_system.core.services.admin.OrderRequestService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final OrderRequestService orderRequestService;

    @SneakyThrows
    @GetMapping(value = "/order-request-log", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<OrderRequestLogDto>> getOrderRequestLog(@RequestParam(value = "managerId", required = false) Long managerId,
                                                                       @RequestParam(value = "authorId", required = false) Long authorId,
                                                                       @RequestParam(value = "order", required = false) String order,
                                                                       @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(orderRequestService.find(managerId, authorId, order, pageable));
    }
}
