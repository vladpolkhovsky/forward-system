package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.dto.rest.manager.ManagerOrderDto;
import by.forward.forward_system.core.services.NewOrderService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/manager")
@RequiredArgsConstructor
public class MangerRestController {

    private final NewOrderService newOrderService;

    @GetMapping(value = "/get-manager-orders")
    public ResponseEntity<List<ManagerOrderDto>> getAuthorOrders(@RequestParam(name = "showClosed", defaultValue = "false") Boolean showClosed) {
        var orders = newOrderService.getManagerOrders(AuthUtils.getCurrentUserId(), showClosed);
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/get-manager-orders/{authorId}")
    public ResponseEntity<List<ManagerOrderDto>> getAuthorOrdersById(@PathVariable Long authorId,
                                                                     @RequestParam(name = "showClosed", defaultValue = "false") Boolean showClosed) {
        var orders = newOrderService.getManagerOrders(AuthUtils.getCurrentUserId(), showClosed);
        return ResponseEntity.ok(orders);
    }
}
