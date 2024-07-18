package by.forward.forward_system.core.dto.messenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderParticipantDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private String participantsType;
    private String participantsTypeRus;
}
