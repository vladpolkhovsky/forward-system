package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.fast.LoadChatByIdRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.LoadChatRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.LoadChatResponseDto;
import by.forward.forward_system.core.dto.messenger.fast.SearchChatRequestDto;
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
}
