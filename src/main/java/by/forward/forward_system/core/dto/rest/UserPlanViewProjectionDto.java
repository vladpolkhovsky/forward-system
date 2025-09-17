package by.forward.forward_system.core.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPlanViewProjectionDto {
    private Long planId;
    private String planStart;
    private String planEnd;
    private Boolean isPlanNotStartYet;
    private Boolean isPlanActive;
    private Long beforePlanBeginDays;
    private Long beforePlanBeginHours;
    private Long userId;
    private String username;
    private Double targetSum;
    private Double orderSum;
    private Double orderSumPercent;
    private Double targetCount;
    private Double orderCountSum;
    private Double orderCountSumPercent;
}
