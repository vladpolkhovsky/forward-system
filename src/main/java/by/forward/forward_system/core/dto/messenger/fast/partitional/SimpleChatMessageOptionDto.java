package by.forward.forward_system.core.dto.messenger.fast.partitional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleChatMessageOptionDto {
    private long id;
    private long messageId;
    private String url;
    private String name;
}
