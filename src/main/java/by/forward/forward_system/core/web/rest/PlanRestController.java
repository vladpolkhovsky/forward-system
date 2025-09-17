package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.UserPlanViewProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.UserSimpleProjectionDto;
import by.forward.forward_system.core.services.core.PlanService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/plan")
@AllArgsConstructor
public class PlanRestController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final PlanService planService;

    @GetMapping(value = "/managers", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<UserSimpleProjectionDto>> getAllManagers() {
        return ResponseEntity.ok(planService.getAllManagers());
    }

    @GetMapping(value = "/user-plans/{userId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<UserPlanProjectionDto>> getAllUserPlans(@PathVariable Long userId) {
        return ResponseEntity.ok(planService.getAllUserPlans(userId));
    }

    @PostMapping(value = "/validate", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<PlanService.ValidationResponse> validatePlan(@RequestBody PlanService.ValidationRequest validationRequest) {
        return ResponseEntity.ok(planService.validate(validationRequest));
    }

    @GetMapping(value = "/user-plan-view")
    public ResponseEntity<List<UserPlanViewProjectionDto>> findAllUserPlans() {
        return ResponseEntity.ok(planService.findAllUserPlanViews());
    }

    @GetMapping(value = "/user-plan-view/{userId}")
    public ResponseEntity<Optional<UserPlanViewProjectionDto>> findActiveUserPlan(@PathVariable Long userId) {
        return ResponseEntity.ok(planService.findActiveUserPlanById(userId));
    }
}
