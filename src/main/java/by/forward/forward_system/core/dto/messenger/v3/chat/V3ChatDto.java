package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.dto.messenger.v3.ChatMetadataDto;
import by.forward.forward_system.core.dto.messenger.v3.ChatTagDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.OrderStatus;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ChatDto {
    private Long id;
    private String chatName;
    private String displayName;
    private String lastMessageDate;
    private String realLastMessageDate;
    @With
    private Integer newMessageCount;
    private List<ChatTagDto> tags;
    private Long orderId;
    private String orderTechNumber;
    private OrderStatus orderStatus;
    private String orderStatusRus;
    private ChatType type;
    private ChatMetadataDto metadata;
}
