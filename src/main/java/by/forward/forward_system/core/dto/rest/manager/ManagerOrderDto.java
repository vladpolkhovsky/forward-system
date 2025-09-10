package by.forward.forward_system.core.dto.rest.manager;

import by.forward.forward_system.core.dto.messenger.v3.V3ParticipantDto;
import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerOrderDto {
    private long orderId;
    private String orderTechNumber;
    private String subject;
    private int originality;
    private List<AdditionalDateDto> additionalDates;
    private List<V3ParticipantDto> participants;
    private String deadline;
    private String intermediateDeadline;
    private String orderStatus;
    private String orderStatusRus;
}
