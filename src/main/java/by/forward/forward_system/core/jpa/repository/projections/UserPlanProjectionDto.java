package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.jpa.model.PlanEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class UserPlanProjectionDto {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Long id;
    private final Long userId;
    private final String username;
    private final String start;
    private final String end;
    private final String planName;
    private final String planNameShort;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Long targetSum;
    private final Long targetCount;

    public UserPlanProjectionDto(PlanEntity planEntity, UserEntity userEntity) {
        this(
            planEntity.getId(),
            planEntity.getUserId(),
            userEntity.getUsername(),
            planEntity.getStart(),
            planEntity.getEnd(),
            planEntity.getTargetSum(),
            planEntity.getTargetCount()
        );
    }

    public UserPlanProjectionDto(Long id,
                                 Long userId,
                                 String username,
                                 LocalDateTime start,
                                 LocalDateTime end,
                                 Long targetSum,
                                 Long targetCount
    ) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.targetSum = targetSum;
        this.targetCount = targetCount;
        this.start = start.format(DATE_TIME_FORMATTER);
        this.end = end.toLocalDate().atStartOfDay().plusDays(1).format(DATE_TIME_FORMATTER);

        this.startDateTime = start;
        this.endDateTime = end;

        this.planName = "План (для %s) с %s по %s".formatted(
            username,
            start.format(DEFAULT_FORMAT),
            end.format(DEFAULT_FORMAT)
        );

        this.planNameShort = "C %s по %s".formatted(
            start.format(DEFAULT_FORMAT),
            end.format(DEFAULT_FORMAT)
        );
    }

    @JsonProperty("allDay")
    public boolean allDay() {
        return true;
    }

    @JsonProperty("title")
    public String title() {
        return planName;
    }

    @JsonProperty("editable")
    public boolean editable() {
        return false;
    }

    @JsonProperty("beforeStartDays")
    public Long untilStartDays(LocalDateTime now) {
        return (long) Math.ceil(Duration.between(now, startDateTime).toHours() / 24.0);
    }
}
