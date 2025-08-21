package by.forward.forward_system.core.dto.messenger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberDto {
    private Long chatId;
    private Long userId;
}
