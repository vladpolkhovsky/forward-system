package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.messenger.UserDto;
import by.forward.forward_system.core.dto.rest.AttachmentDto;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<Map<String, Boolean>> viewMessage(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.setMessageViewed(chatId, userId);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @PostMapping(value = "/message-viewed/{chatId}")
    public ResponseEntity<Map<String, Boolean>> viewMessage(@PathVariable Long chatId) {
        chatService.setMessageViewed(chatId, AuthUtils.getCurrentUserId());
        return ResponseEntity.ok(Map.of("status", true));
    }

    @Deprecated
    @PostMapping(value = "/file-save", consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Long> saveFile(@RequestBody AttachmentDto attachment) {
        List<Long> longs = attachmentService.saveAttachment(Arrays.asList(attachment));
        return ResponseEntity.ok(longs.get(0));
    }

    @SneakyThrows
    @PostMapping(value = "/file-save-form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> saveFileForm(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(attachmentService.saveAttachmentRaw(file.getOriginalFilename(), file.getBytes()));
    }

    @SneakyThrows
    @Transactional
    @PostMapping(value = "/send-message-via-http", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> sendMessageViaHttp(@RequestParam(value = "file", required = false) MultipartFile file,
                                                   @RequestParam(value = "messageText", required = false) String messageText,
                                                   @RequestParam(value = "chatId") Long chatId,
                                                   @RequestParam(value = "userId") Long userId) {
        Optional<AttachmentEntity> fileEntity = Optional.ofNullable(file)
            .map(this::saveMultipartFile);

        return ResponseEntity.ok(
            chatService.sendMessageViaHttp(fileEntity, messageText, chatId, userId)
        );
    }

    @SneakyThrows
    private AttachmentEntity saveMultipartFile(MultipartFile t) {
        return attachmentService.saveAttachment(t.getOriginalFilename(), t.getBytes());
    }
}
