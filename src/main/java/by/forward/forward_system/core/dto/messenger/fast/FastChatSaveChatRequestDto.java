package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FastChatSaveChatRequestDto {
    private long chatId;
    private long userId;
}
