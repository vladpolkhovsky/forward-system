package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadChatInfoResponseDto {
    private long chatId;
    private String chatName;
    private Long orderId;
    private OrderInfoDto order;
    private List<FastChatMemberDto> members;
    private ChatMetadataDto metadata;
    private List<FastChatMessageDto> messages;
    private long size;
}
