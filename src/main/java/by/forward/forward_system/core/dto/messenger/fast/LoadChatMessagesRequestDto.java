package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadChatMessagesRequestDto {
    private long chatId;
    private long userId;
    private List<Long> loaded;
    private long size;
}
