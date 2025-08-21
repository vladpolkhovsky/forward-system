package by.forward.forward_system.core.web.rest.chat;

import by.forward.forward_system.core.dto.messenger.v3.chat.*;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.v3.V3ChatLoadService;
import by.forward.forward_system.core.services.v3.V3ChatMessageLoadService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v3/chat")
@AllArgsConstructor
public class V3ChatRestController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final V3ChatLoadService v3ChatLoadService;
    private final V3ChatMessageLoadService v3ChatMessageLoadService;
    private final ChatService chatService;
    private final MessageService messageService;

    @PostMapping(value = "/search", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Page<V3ChatDto>> search(@RequestBody V3ChatSearchCriteria criteria, @PageableDefault(size = 50, page = 0) Pageable pageable) {
        return ResponseEntity.ok(v3ChatLoadService.search(criteria, pageable));
    }

    @GetMapping(value = "/message", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Page<V3MessageDto>> search(@RequestParam("chatId") Long chatId, @RequestParam("after") LocalDateTime after, @PageableDefault(size = 25, page = 0) Pageable pageable) {
        var criteria = V3MessageSearchCriteria.builder().chatId(chatId);
        Optional.ofNullable(after).ifPresent(criteria::afterTime);
        return ResponseEntity.ok(v3ChatMessageLoadService.search(criteria.build(), pageable));
    }

    @GetMapping(value = "/id/{chatId}", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<V3ChatDto> findById(@PathVariable Long chatId) {
        return ResponseEntity.ok(v3ChatLoadService.findById(chatId));
    }

    @GetMapping(value = "/message/id/{messageId}", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<V3MessageDto> findMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(v3ChatMessageLoadService.findById(messageId));
    }

    @PostMapping(value = "/send", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Long>> sendMessageToChat(@RequestBody V3SendMessageDto request) {
        Long messageId = messageService.sendMessage(request.getUserId(), request.getChatId(), request.getText(), request.getAttachmentsIds());
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }

    @GetMapping(value = "/count", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Long>> getCount() {
        return ResponseEntity.ok(v3ChatLoadService.getNewMessageCount(AuthUtils.getCurrentUserId()));
    }
}
