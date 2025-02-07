package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.ChatMemberDto;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.ChatMemberEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatMemberRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ChatMemberService {

    private final ChatMemberRepository chatMemberRepository;

    public ChatMemberEntity addMemberToChat(UserEntity userEntity, ChatEntity chatEntity) {
        for (ChatMemberEntity chatMember : chatEntity.getChatMembers()) {
            if (chatMember.getUser().getId().equals(userEntity.getId())) {
                return chatMember;
            }
        }
        ChatMemberEntity chatMemberEntity = new ChatMemberEntity();
        chatMemberEntity.setUser(userEntity);
        chatMemberEntity.setChat(chatEntity);
        return chatMemberRepository.save(chatMemberEntity);
    }

    public List<ChatMemberDto> getChatMembers(ChatEntity chatEntity) {
        List<ChatMemberDto> chatMembers = new ArrayList<>();
        for (ChatMemberEntity chatMember : chatEntity.getChatMembers()) {
            ChatMemberDto chatMemberDto = new ChatMemberDto();
            chatMemberDto.setId(chatMember.getId());
            chatMemberDto.setUserId(chatMember.getUser().getId());
            chatMemberDto.setChatId(chatMember.getChat().getId());
            chatMembers.add(chatMemberDto);
        }
        return chatMembers;
    }

}
