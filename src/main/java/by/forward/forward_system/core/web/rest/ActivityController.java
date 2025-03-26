package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import by.forward.forward_system.core.services.core.UserActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final static String TEXT_HTML = "text/html; charset=UTF-8";

    private final UserActivityService userActivityService;

    @GetMapping(value = "/ping/{userId}", consumes = JSON_MEDIA_TYPE, produces = TEXT_HTML)
    public ResponseEntity<String> ping(@PathVariable Long userId) {
        userActivityService.updateUserActivity(userId);
        return ResponseEntity.status(HttpStatus.OK)
            .body("""
                <h4>Вы не должны открывать эту страницу!</h4>
                <p>Эта страница запрашивается при обновлении вашего статуса в чате. Тут делать нечего.</p>
                <a href="/main">Вернуться на главную<a>
                """);
    }

    @GetMapping(value = "/all/activity", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<UserActivityDto>> getAllActivity() {
        return ResponseEntity.ok().body(userActivityService.getAllUserActivity());
    }
}
