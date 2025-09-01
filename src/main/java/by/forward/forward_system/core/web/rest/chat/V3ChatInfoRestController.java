package by.forward.forward_system.core.web.rest.chat;

import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ChatOrderInfoDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ForwardOrderChatInfo;
import by.forward.forward_system.core.dto.messenger.v3.V3OrderReviewDto;
import by.forward.forward_system.core.services.core.ForwardOrderService;
import by.forward.forward_system.core.services.ui.UserUiService;
import by.forward.forward_system.core.services.v3.V3ChatInfoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v3/chat/info")
@AllArgsConstructor
public class V3ChatInfoRestController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final V3ChatInfoService v3ChatInfoService;

    private final UserUiService userUiService;
    private final ForwardOrderService forwardOrderService;

    @GetMapping(value = "/order/{orderId}", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<V3ChatOrderInfoDto> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(v3ChatInfoService.loadOrderInfo(orderId));
    }

    @GetMapping(value = "/order/forward/{orderId}", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<V3ForwardOrderChatInfo> findForwardOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(v3ChatInfoService.loadForwardOrderInfo(orderId));
    }

    @GetMapping(value = "/order/review/{orderId}", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<V3OrderReviewDto>> findOrderReviewsById(@PathVariable Long orderId) {
        return ResponseEntity.ok(v3ChatInfoService.loadOrderReviewsInfo(orderId));
    }

    @PostMapping("/order/forward/delete-all-telegram-chat-customers/{forwardOrderId}")
    public ResponseEntity<Map<String, Boolean>> saveAdminNote(@PathVariable Long forwardOrderId) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.deleteAllFromTelegramChat(forwardOrderId, currentUserId);
        return ResponseEntity.ok(Map.of("status", true));
    }
}
