package by.forward.forward_system.core.dto.messenger.fast.partitional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleChatInfoDto {
    private long id;
    private Long orderId;
    private String name;
    private String chatType;
    private boolean onlyOwnerCanType;
}
