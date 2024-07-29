package by.forward.forward_system.core.jpa.repository.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatAttachmentProjectionDto {
    private String fio;
    private Long attachmentFileId;
    private String attachmentFilename;
    private LocalDateTime attachmentTime;
    private Boolean checked;
}
