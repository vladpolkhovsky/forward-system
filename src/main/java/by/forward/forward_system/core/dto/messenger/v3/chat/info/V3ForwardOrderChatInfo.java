package by.forward.forward_system.core.dto.messenger.v3.chat.info;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ForwardOrderChatInfo {
    private Long forwardOrderId;
    private Long orderId;
    private String code;
    private Long botCount;
    private Boolean isOrderPaid;
    private Boolean isAuthorCanSubmitFiles;
}
