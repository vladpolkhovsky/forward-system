package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.AddParticipantRequestDto;
import by.forward.forward_system.core.services.core.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/order")
@AllArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping(value = "/add-participants/{orderId}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Boolean> addParticipant(@PathVariable("orderId") Long orderId,
                                                  @RequestBody AddParticipantRequestDto addParticipantRequestDto) {
        orderService.addParticipant(orderId, addParticipantRequestDto);
        return ResponseEntity.ok(true);
    }

}
