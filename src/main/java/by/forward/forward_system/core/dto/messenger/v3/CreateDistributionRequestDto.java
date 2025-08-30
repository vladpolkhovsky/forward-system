package by.forward.forward_system.core.dto.messenger.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDistributionRequestDto {
    private String text;
    private Boolean isQueueDistribution;
    private Long queueDistributionWaitMinutes;
    private List<DistributionPersonDto> persons;
}
