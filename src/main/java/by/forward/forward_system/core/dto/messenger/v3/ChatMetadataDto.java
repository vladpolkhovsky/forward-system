package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMetadataDto {
    private Long chatId;
    private ChatType chatType;
    private Boolean onlyOwnerCanType;
    private Boolean authorCanSubmitFiles;
    private Long managerId;
    private Long authorId;
}
