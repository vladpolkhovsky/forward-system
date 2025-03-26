package by.forward.forward_system.core.jpa.repository.projections;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface ReviewRequestProjection {

    DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    Long getId();
    Long getForwardOrderId();
    String getNote();
    String getAuthorUsername();
    Boolean getDone();
    Long getFileId();
    LocalDateTime getCreatedAt();

    Long getReviewId();
    String getReviewMark();
    Boolean getReviewIsReviewed();
    LocalDateTime getReviewReviewedAt();

    default String getReviewStatusString() {
        if (getReviewId() == null) {
            return "Задание на проверку не создано.";
        }
        String status = "Не проверено";
        if (getReviewIsReviewed()) {
            status = "Проверено. Оценка: " + getReviewMark();
        }
        return "Открыть карточку запроса (%s)"
            .formatted(status);
    }

    default String getCreatedAtString() {
        return getCreatedAt().format(outputDateTimeFormat);
    }

    default String getReviewReviewedAtString() {
        return getReviewReviewedAt().format(outputDateTimeFormat);
    }
}
