package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.messenger.UserDto;
import by.forward.forward_system.core.dto.rest.AttachmentDto;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.services.messager.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/messenger")
@AllArgsConstructor
public class MessengerRestController {

    private final ChatService chatService;

    private final OrderService orderService;

    private final UserService userService;

    private final AttachmentService attachmentService;

    @GetMapping(value = "/{userId}/chats")
    public ResponseEntity<List<ChatDto>> getUserChats(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUserChats(userId));
    }

    @GetMapping(value = "/{userId}/chats/{chatId}")
    public ResponseEntity<ChatDto> getUserChat(@PathVariable Long userId, @PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getUserChat(userId, chatId));
    }

    @GetMapping(value = "/{userId}/orders")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping(value = "/orders")
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping(value = "/orders/{orderId}")
    public ResponseEntity<OrderDto> getSingleOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getSingleOrder(orderId));
    }

    @GetMapping(value = "/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsersConverted());
    }

    @PostMapping(value = "/hide-message/{messageId}")
    private ResponseEntity<String> hideMessage(@PathVariable Long messageId) {
        chatService.hideMessage(messageId);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/message-viewed/{chatId}/{userId}")
    public ResponseEntity<Boolean> viewMessage(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.setMessageViewed(chatId, userId);
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/file-save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> saveFile(@RequestBody AttachmentDto attachment) {
        List<Long> longs = attachmentService.saveAttachment(Arrays.asList(attachment));
        return ResponseEntity.ok(longs.get(0));
    }
}
