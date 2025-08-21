package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3MessageDto {
    private Long id;
    private Long chatId;
    private Boolean isSystemMessage;
    private Long fromUserId;
    private String fromUserUsername;
    private String fromUserOrderParticipantType;
    private String fromUserOrderParticipantTypeRusName;
    private Boolean fromUserIsAdmin;
    private String createdAt;
    private String realCreatedAt;
    private Boolean isNewMessage;
    private ChatMessageType messageType;
    private List<String> messageReadedByUsernames;
    private String text;
    private List<V3OptionDto> options;
    private List<V3AttachmentDto> attachments;
}
