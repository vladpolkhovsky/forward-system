package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.ChatDto;
import by.forward.forward_system.core.services.messager.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatUtilsService {

    private final ChatService chatService;

    public void addAdminToAllChats(Long id) {
        List<ChatDto> adminTalkChats = chatService.getAllChats();
        List<Long> chatIds = adminTalkChats.stream()
            .map(ChatDto::getId)
            .toList();
        chatService.addUserToChats(
            chatIds,
            id
        );
    }
}
