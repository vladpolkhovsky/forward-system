package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMemberDto {
    private Long id;
    private Long chatId;
    private Long userId;
}
