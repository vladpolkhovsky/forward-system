package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.fast.*;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3NewMessageDto;
import by.forward.forward_system.core.services.newchat.FastChatService;
import by.forward.forward_system.core.services.v3.V3ChatMessageLoadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/new-chat")
@AllArgsConstructor
public class FastMessengerController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final FastChatService fastChatService;
    private final V3ChatMessageLoadService v3ChatMessageLoadService;

    @PostMapping(value = "/chats", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatResponseDto> loadChat(@RequestBody LoadChatRequestDto loadChatRequestDto) {
        return ResponseEntity.ok(fastChatService.loadChats(loadChatRequestDto));
    }

    @PostMapping(value = "/search", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatResponseDto> searchChat(@RequestBody SearchChatRequestDto searchChatRequestDto) {
        return ResponseEntity.ok(fastChatService.searchChats(searchChatRequestDto));
    }

    @PostMapping(value = "/chat-by-id", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatResponseDto> loadChatById(@RequestBody LoadChatByIdRequestDto loadChatByIdRequestDto) {
        return ResponseEntity.ok(fastChatService.loadChatById(loadChatByIdRequestDto));
    }

    @PostMapping(value = "/load-chat-info", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatInfoResponseDto> loadChatInfo(@RequestBody LoadChatInfoRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.loadChatInfo(requestDto));
    }

    @PostMapping(value = "/messages", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatMessagesResponseDto> loadChatMessages(@RequestBody LoadChatMessagesRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.loadChatMessages(requestDto));
    }

    @PostMapping(value = "/new-message-info", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<NewMessageCountResponseDto> loadNewMessageCount(@RequestBody NewMessageCountRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.loadNewMessageCount(requestDto));
    }

    @PostMapping(value = "/load-order-status", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadChatsStatusResponseDto> loadChatStatusByChats(@RequestBody LoadChatsStatusRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.loadChatStatus(requestDto));
    }

    @PostMapping(value = "/load-order-member", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<LoadIsChatMemberResponseDto> loadChatMemberByChats(@RequestBody LoadIsChatMemberRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.loadIsChatMember(requestDto));
    }

    @PostMapping(value = "/update-saved-chat", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<FastChatSaveChatResponseDto> updateSavedChat(@RequestBody FastChatSaveChatRequestDto requestDto) {
        return ResponseEntity.ok(fastChatService.changeSavedStatus(requestDto));
    }

    @PostMapping(value = "/who-read-message/{messageId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<String>> loadWhoReadMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(fastChatService.loadWhoReadMessage(messageId));
    }

    private static final DateTimeFormatter dateTimeFormatterSmallDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");

    @PostMapping(value = "/new-messages-ids-by-http", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    @Deprecated
    public ResponseEntity<List<Long>> newMessageIdsByHttp(@RequestBody LoadNewChatIdsRequestDto requestDto) {
        var after = Optional.ofNullable(requestDto.getAfter())
                .map(t -> LocalDateTime.parse(t, dateTimeFormatterSmallDate))
                .orElse(LocalDateTime.now().minusMinutes(5));
        return ResponseEntity.ok(fastChatService.newMessageIdsByHttp(requestDto.getChatId(), requestDto.getUserId(), after));
    }

    @GetMapping(value = "/new-messages-ids-by-http/{userId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<V3NewMessageDto>> newMessageIdsByHttp(@RequestParam("after") LocalDateTime after, @PathVariable Long userId) {
        return ResponseEntity.ok(v3ChatMessageLoadService.findNewMessagesForUserAfter(userId, null, after));
    }

    @GetMapping(value = "/new-messages-ids-by-http/{userId}/{chatId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<V3NewMessageDto>> newMessageIdsByHttp(@RequestParam("after") LocalDateTime after, @PathVariable Long userId, @PathVariable Long chatId) {
        return ResponseEntity.ok(v3ChatMessageLoadService.findNewMessagesForUserAfter(userId, chatId, after));
    }

    @GetMapping(value = "/server-time", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, String>> getServerTime() {
        return ResponseEntity.ok(Map.of("time", LocalDateTime.now().toString()));
    }

    @PostMapping(value = "/messages-by-ids-http/{userId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    @Deprecated
    public ResponseEntity<List<FastChatMessageDto>> loadMessagesByIds(@PathVariable Long userId, @RequestBody List<Long> messageIds) {
        return ResponseEntity.ok(fastChatService.loadChatMessagesByIds(messageIds, userId));
    }
}
