package by.forward.forward_system.core.jpa.repository.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class UserActivityDto {
    private static final DateTimeFormatter lessThen12hours = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter moreThen12hours = DateTimeFormatter.ofPattern("dd.MM' в 'HH:mm");

    public UserActivityDto(Long id, String username, LocalDateTime lastSeenDate) {
        this.id = id;
        this.username = "hashcode: " + username.hashCode();
        LocalDateTime now = LocalDateTime.now();

        if (lastSeenDate == null) {
            lastOnlineAt = "Никогда";
            online = false;
            return;
        }

        if (now.minusMinutes(1).isBefore(lastSeenDate)) {
            this.lastOnlineAt = "Онлайн";
            online = true;
        } else {
            this.lastOnlineAt = lastSeenDate.format(lessThen12hours);
        }

        if (now.minusHours(12).isAfter(lastSeenDate)) {
            this.lastOnlineAt = lastSeenDate.format(moreThen12hours);
        }
    }

    private long id;
    private String username;
    private String lastOnlineAt;
    private boolean online;
}
