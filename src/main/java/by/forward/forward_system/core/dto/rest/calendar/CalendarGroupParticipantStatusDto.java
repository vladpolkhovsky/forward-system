package by.forward.forward_system.core.dto.rest.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarGroupParticipantStatusDto {
    private Long groupId;
    private Map<String, List<Long>> days;
}
