package by.forward.forward_system.core.dto.ui;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private Long orderId;
    private Long attachmentId;
    private Long additionalAttachmentId;
    private String expertUsername;
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
