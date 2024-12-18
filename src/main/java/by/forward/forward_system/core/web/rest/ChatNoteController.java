package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.fast.ChatNoteDto;
import by.forward.forward_system.core.services.newchat.FastChatService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/new-chat")
@AllArgsConstructor
public class ChatNoteController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final FastChatService fastChatService;

    @PostMapping(value = "/notes/{userId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<ChatNoteDto>> getAllNotes(@PathVariable Long userId) {
        return ResponseEntity.ok(fastChatService.loadAllChatNotes(userId));
    }

    @PostMapping(value = "/notes/{userId}/new", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<ChatNoteDto>> getAllNotes(@PathVariable Long userId,
                                                         @RequestBody ChatNoteDto chatNoteDto) {
        return ResponseEntity.ok(fastChatService.createNote(userId, chatNoteDto));
    }

    @PostMapping(value = "/notes/{userId}/{noteId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<ChatNoteDto>> updateNote(@PathVariable Long userId,
                                                        @PathVariable Long noteId,
                                                        @RequestBody ChatNoteDto chatNoteDto) {
        return ResponseEntity.ok(fastChatService.updateChatNote(userId, noteId, chatNoteDto));
    }

    @DeleteMapping(value = "/notes/{userId}/{noteId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<ChatNoteDto>> deleteNote(@PathVariable Long userId,
                                                        @PathVariable Long noteId) {
        return ResponseEntity.ok(fastChatService.deleteChatNote(userId, noteId));
    }
}
