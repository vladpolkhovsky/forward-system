package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import by.forward.forward_system.core.dto.rest.users.UserDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3OrderFullDto {
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
    @With
    private Long orderChatId;
    @With
    private Long orderChatIdNewMessages;
    private String createdAt;
    private UserDto createdBy;
    private Long distributionDays;
}
