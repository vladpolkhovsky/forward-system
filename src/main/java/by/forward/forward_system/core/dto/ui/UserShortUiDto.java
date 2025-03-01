package by.forward.forward_system.core.dto.ui;

import by.forward.forward_system.core.services.core.PlanService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserShortUiDto {
    private Long id;
    private String username;
    private String fio;
    private String rolesRus;
    private List<PlanService.UserPlanDetailsDto> plans;

    public boolean hasPlans() {
        return plans != null && !plans.isEmpty();
    }
}
