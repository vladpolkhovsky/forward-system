package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatNoteDto {
    private Long id;
    private Long userId;
    private Long chatId;
    private String chatName;
    private String chatTab;
    private String text;
    private String createdAt;
}
