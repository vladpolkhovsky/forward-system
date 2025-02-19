package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.core.CalendarUserEventsService;
import by.forward.forward_system.core.services.core.CalendarUserEventsService.CalendarUserEventsTransformer;
import by.forward.forward_system.core.services.core.CalendarUserEventsService.CalendarUserEventsTransformer.JsonCalendarDto;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@AllArgsConstructor
public class CalendarController {

    private final UserUiService userUiService;

    @GetMapping(value = "/calendar")
    public String calendar(Model model) {

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("userId", userUiService.getCurrentUserId());
        model.addAttribute("menuName", "Календарь сдачи работ");

        return "main/calendar/calendar";
    }

    @RestController
    @AllArgsConstructor
    public static class CalendarRestController {

        private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

        private final CalendarUserEventsService calendarUserEventsService;
        private final CalendarUserEventsTransformer calendarUserEventsTransformer;

        @GetMapping(value = "/calendar/user/{userId}/events", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
        public ResponseEntity<List<JsonCalendarDto>> getUserEvents(@PathVariable Long userId) {
            return ResponseEntity.ok(calendarUserEventsTransformer.toJsonEvents(calendarUserEventsService.getUserEvents(userId)));
        }
    }
}
