package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3OrderDto {
    private Long id;
    private String techNumber;
    private String subject;
    private String disciplineName;
    private Long disciplineId;
    private Long originality;
    private String deadline;
    private String intermediateDeadline;
    private List<AdditionalDateDto> additionalDates;
    private List<V3ParticipantDto> participants;
    private String orderStatus;
    private String orderStatusRus;
    private BigDecimal orderCost;
    private BigDecimal orderAuthorCost;
}
