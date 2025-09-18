package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.ParticipantType;
import lombok.*;

import java.util.List;
import java.util.Set;

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
    private Boolean fromUserIsDeleted;
    private String createdAt;
    private String realCreatedAt;
    private ChatMessageType messageType;
    @With
    private Set<String> messageReadedByUsernames;
    @With
    private Boolean isNewMessage;
    private String text;
    private List<V3OptionDto> options;
    private List<V3AttachmentDto> attachments;
}
