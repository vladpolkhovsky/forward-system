package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.calendar.AuthorToGroupRequestDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupParticipantStatusDto;
import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupParticipantStatusSwitchDto;
import by.forward.forward_system.core.services.NewCalendarService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping(value = "/api/calendar")
@AllArgsConstructor
public class CalendarRestController {

    private final NewCalendarService newCalendarService;

    @PostMapping("/add-to-group")
    public ResponseEntity<Void> addToGroup(@RequestBody AuthorToGroupRequestDto request) {
        newCalendarService.addUserToGroupRequest(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-from-group")
    public ResponseEntity<Void> deleteFromGroup(@RequestBody AuthorToGroupRequestDto request) {
        newCalendarService.deleteFromGroup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-group")
    public ResponseEntity<Void> deleteFromGroup(@RequestBody CalendarGroupDto request) {
        newCalendarService.deleteGroup(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-groups")
    public ResponseEntity<List<CalendarGroupDto>> getGroups() {
        var groups = newCalendarService.getGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/get-group/{groupId}")
    public ResponseEntity<CalendarGroupDto> getGroup(@PathVariable Long groupId) {
        var groups = newCalendarService.getGroup(groupId);
        return ResponseEntity.of(groups);
    }

    @GetMapping("/get-group-participant-status/{groupId}")
    public ResponseEntity<CalendarGroupParticipantStatusDto> getGroup(@PathVariable Long groupId,
                                                                      @RequestParam("begin") String begin,
                                                                      @RequestParam("end") String end) {
        LocalDate beginDate = LocalDate.parse(begin, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        var groups = newCalendarService.getGroupParticipantsStatus(groupId, beginDate, endDate);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/get-group-participant-status/switch/{groupId}/{userId}")
    public ResponseEntity<CalendarGroupParticipantStatusSwitchDto> switchParticipantStatus(@PathVariable Long groupId,
                                                                                           @PathVariable Long userId,
                                                                                           @RequestParam("date") String date) {
        LocalDate dateDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        var groups = newCalendarService.switchGroupParticipantStatus(groupId, userId, dateDate);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/create-group")
    public ResponseEntity<CalendarGroupDto> getGroup(@RequestBody CalendarGroupDto request) {
        return ResponseEntity.ok(newCalendarService.save(request));
    }

}
