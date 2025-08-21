package by.forward.forward_system.core.dto.messenger.v3.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3SendMessageDto {
    private Long chatId;
    private Long userId;
    private String text;
    private List<Long> attachmentsIds;
}
