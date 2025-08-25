package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
public class UserActivityDto {
    private static final String lessThen55minutesFormat = "%d мин. назад";
    private static final DateTimeFormatter lessThen12hours = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter moreThen12hours = DateTimeFormatter.ofPattern("dd.MM' в 'HH:mm");

    public UserActivityDto(Long id, String username, LocalDateTime lastSeenDate) {
        this.id = id;
        this.shouldBeVisible = true;

        if (ChatNames.SYSTEM_USERS.contains(id)) {
            this.online = true;
            this.lastOnlineAt = "Онлайн";
            this.shouldBeVisible = false;
            return;
        }

        this.username = "hashcode: " + username.hashCode();
        LocalDateTime now = LocalDateTime.now();

        if (lastSeenDate == null) {
            this.lastOnlineAt = "Никогда";
            this.online = false;
            return;
        }

        if (lastSeenDate.isAfter(now.minusMinutes(1).minusSeconds(2))) {
            this.lastOnlineAt = "Онлайн";
            this.online = true;
            return;
        }

        if (lastSeenDate.isAfter(now.minusMinutes(55))) {
            this.lastOnlineAt = lessThen55minutesFormat.formatted(ChronoUnit.MINUTES.between(lastSeenDate, now));
        } else if (lastSeenDate.isAfter(now.minusHours(12))) {
            this.lastOnlineAt = lastSeenDate.format(lessThen12hours);
        } else {
            this.lastOnlineAt = lastSeenDate.format(moreThen12hours);
        }

        this.online = false;
    }

    private long id;
    private String username;
    private String lastOnlineAt;
    private boolean online;
    private boolean shouldBeVisible;
}
