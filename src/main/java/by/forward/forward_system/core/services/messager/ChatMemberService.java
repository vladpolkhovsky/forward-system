package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.ChatMemberDto;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class ChatMemberService {

    public ChatRepository chatRepository;

    @Transactional
    public void addMemberToChat(UserEntity userEntity, ChatEntity chatEntity) {
        chatEntity.getParticipants().add(userEntity);
    }

    @Transactional
    public List<ChatMemberDto> getChatMembers(ChatEntity chatEntity) {
        return chatEntity.getParticipants().stream()
                .map(t -> new ChatMemberDto(chatEntity.getId(), t.getId()))
                .toList();
    }
}
