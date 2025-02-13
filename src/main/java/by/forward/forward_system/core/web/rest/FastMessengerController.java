package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.fast.*;
import by.forward.forward_system.core.services.newchat.FastChatService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/new-chat")
@AllArgsConstructor
public class FastMessengerController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final FastChatService fastChatService;

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
}
