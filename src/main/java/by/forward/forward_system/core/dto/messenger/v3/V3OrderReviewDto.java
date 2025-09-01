package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3AttachmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3OrderReviewDto {
    private Long id;
    private Long orderId;
    private String orderTechNumber;
    private Boolean isApproved;
    private String requestText;
    private String resultText;
    private String resultMark;
    private String resultDate;
    private String createdAt;
    private V3AttachmentDto requestFile;
    private V3AttachmentDto resultFile;
}
