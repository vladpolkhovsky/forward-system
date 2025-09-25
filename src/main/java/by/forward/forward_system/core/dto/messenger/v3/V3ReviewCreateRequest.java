package by.forward.forward_system.core.dto.messenger.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ReviewCreateRequest {
    private Long orderId;
    private Long fileId;
    private Long chatId;
    private String requestText;
}
