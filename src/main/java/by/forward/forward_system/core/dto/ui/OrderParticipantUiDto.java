package by.forward.forward_system.core.dto.ui;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderParticipantUiDto {
    private Long id;
    private String userName;
    private String fio;
    private String participantType;
    private String participantTypeRus;
    private Double fee;
}
