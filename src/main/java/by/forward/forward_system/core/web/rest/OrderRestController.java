package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.v3.V3OrderDto;
import by.forward.forward_system.core.dto.messenger.v3.V3OrderFullDto;
import by.forward.forward_system.core.dto.rest.AddParticipantRequestDto;
import by.forward.forward_system.core.dto.rest.search.OrderSearchCriteria;
import by.forward.forward_system.core.services.NewOrderService;
import by.forward.forward_system.core.services.core.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/order")
@AllArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    private final NewOrderService newOrderService;

    @PostMapping(value = "/add-participants/{orderId}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Boolean> addParticipant(@PathVariable("orderId") Long orderId,
                                                  @RequestBody AddParticipantRequestDto addParticipantRequestDto) {
        orderService.addParticipant(orderId, addParticipantRequestDto);
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<V3OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(newOrderService.getOrderById(id));
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Page<V3OrderFullDto>> search(@PageableDefault(size = 25) Pageable pageable,
                                                       @ModelAttribute OrderSearchCriteria criteria) {
        return ResponseEntity.ok(newOrderService.search(criteria, pageable));
    }
}
