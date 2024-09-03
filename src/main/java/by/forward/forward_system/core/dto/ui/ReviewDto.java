package by.forward.forward_system.core.dto.ui;

import by.forward.forward_system.core.dto.messenger.MessageAttachmentDto;
import by.forward.forward_system.core.dto.rest.AttachmentDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private Long orderId;
    private Long attachmentId;
    private String reviewMessage;
    private String reviewVerdict;
    private String reviewVerdictMark;
    private Long reviewFileId;
    private Boolean isReviewed;
    private Boolean isAccepted;
    private Long reviewedByUserId;
    private LocalDateTime reviewDate;
    private LocalDateTime createdAt;
}
