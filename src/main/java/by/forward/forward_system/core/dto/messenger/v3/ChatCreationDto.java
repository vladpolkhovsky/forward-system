package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreationDto {
    private String chatName;
    private List<Long> members;
    private ChatType chatType;
    private Long orderId;
    private List<ChatTagDto> tags;
    private ChatMetadataDto metadata;
}
