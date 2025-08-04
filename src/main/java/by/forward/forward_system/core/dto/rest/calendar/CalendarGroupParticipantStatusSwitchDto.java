package by.forward.forward_system.core.dto.rest.calendar;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarGroupParticipantStatusSwitchDto {
    private Long groupId;
    private Long userId;
    private String date;
    private boolean isWorking;
}
