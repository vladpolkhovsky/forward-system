package by.forward.forward_system.core.dto.messenger.v3.chat.info;

import by.forward.forward_system.core.dto.rest.AdditionalDateDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ChatOrderInfoDto {
    private Long id;
    private String name;
    private String techNumber;
    private String subject;
    private String status;
    private String statusRusName;
    private Long disciplineId;
    private Long expertGroupId;
    private String expertGroupName;
    private String disciplineName;
    private String deadline;
    private String intermediateDeadline;
    private List<AdditionalDateDto> additionalDates;
    private String authorUsername;
    private String managerUsername;
    private String verificationSystem;
    private Long originality;
}
