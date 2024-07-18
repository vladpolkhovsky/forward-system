package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChatDto {
    private Long id;
    private String chatName;
    private Long orderId;
    private String chatType;
    private String chatTypeRus;
    private LocalDateTime lastMessageTime;
    private List<MessageDto> messages;
    private List<ChatMemberDto> chatMembers;
}
