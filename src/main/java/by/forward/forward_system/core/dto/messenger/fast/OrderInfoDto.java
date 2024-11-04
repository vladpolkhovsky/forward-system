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
public class OrderInfoDto {
    private long id;
    private String workType;
    private String techNumber;
    private String discipline;
    private String orderStatus;
    private String deadline;
    private String intermediateDeadline;
    private String dates;
    private List<OrderParticipantDto> participants;
}
