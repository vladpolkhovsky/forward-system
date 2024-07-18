package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MessageDto {
    private Long id;
    private Long chatId;
    private Long fromUserId;
    private String chatMessageType;
    private Boolean isSystemMessage;
    private String content;
    private LocalDateTime createdAt;
    private List<MessageToUserDto> messageToUser;
    private List<MessageOptionDto> options;
    private List<MessageAttachmentDto> attachments;
}
