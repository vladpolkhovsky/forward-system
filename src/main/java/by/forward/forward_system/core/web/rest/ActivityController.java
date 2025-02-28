package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import by.forward.forward_system.core.services.core.UserActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/activity")
@AllArgsConstructor
public class ActivityController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final UserActivityService userActivityService;

    @GetMapping(value = "/ping/{userId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Void> ping(@PathVariable Long userId) {
        userActivityService.updateUserActivity(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/all/activity", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<UserActivityDto>> getAllActivity() {
        return ResponseEntity.ok().body(userActivityService.getAllUserActivity());
    }
}
