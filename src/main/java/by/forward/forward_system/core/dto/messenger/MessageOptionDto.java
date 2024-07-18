package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageOptionDto {
    private Long id;
    private Long messageId;
    private String orderParticipant;
    private Boolean optionResolved;
    private String content;
    private String optionName;
}
