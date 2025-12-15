package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.UserOrderDates;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CalendarUserEventsService {

    private static final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter outputDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter outputTimeFormat = DateTimeFormatter.ofPattern("HH:mm");

    private final OrderRepository orderRepository;

    private final ObjectMapper objectMapper;

    public List<UserEvent> getUserEvents(Long userId) {
        List<UserOrderDates> userOrderDates = orderRepository.findAllUserOrderEvents(userId);
        return userOrderDates.stream().flatMap(t -> {
                Stream<UserEvent> streamDeadline = parseDeadline(t.getId(), t.getTechNumber(), t.getDeadline()).stream();
                Stream<UserEvent> streamIntermediateDeadline = parseIntermediateDeadline(t.getId(), t.getTechNumber(), t.getIntermediateDeadline()).stream();
                Stream<UserEvent> streamAdditionalDates = parseAdditionalDates(t.getId(), t.getTechNumber(), t.getAdditionalDates()).stream();
                return Stream.concat(streamDeadline, Stream.concat(streamIntermediateDeadline, streamAdditionalDates));
            })
            .sorted(Comparator.comparing(UserEvent::dateTime))
            .toList();
    }

    private List<UserEvent> parseAdditionalDates(Long orderId, String techNumber, String additionalDates) {
        if (StringUtils.isBlank(additionalDates)) {
            return List.of();
        }
        try {
            JsonNode arrayOfDates = objectMapper.readTree(additionalDates);
            List<UserEvent> userEvents = new ArrayList<>();
            for (JsonNode node : arrayOfDates) {
                String description = node.get("text").asText();
                String dateString = node.get("time").asText();
                if (StringUtils.isBlank(dateString)) {
                    continue;
                }
                userEvents.add(createUserEvent(
                    "ТЗ: " + techNumber,
                    description,
                    orderId,
                    parseAdditionalDateString(dateString),
                    UserEventPriority.LOW
                ));
            }
            return userEvents;
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private List<UserEvent> parseIntermediateDeadline(Long orderId, String techNumber, LocalDateTime intermediateDeadline) {
        if (intermediateDeadline == null) {
            return List.of();
        }
        return List.of(createUserEvent(
            "ТЗ: " + techNumber,
            "Промежуточный срок сдачи. Нужно сдать промежуточные файлы работы.",
            orderId,
            intermediateDeadline,
            UserEventPriority.MEDIUM
        ));
    }

    private List<UserEvent> parseDeadline(Long orderId, String techNumber, LocalDateTime deadline) {
        if (deadline == null) {
            return List.of();
        }
        return List.of(createUserEvent(
            "ТЗ: " + techNumber,
            "Окончательный срок сдачи. Нужно сдать финальные файлы работы.",
            orderId,
            deadline,
            UserEventPriority.HIGH
        ));
    }

    private UserEvent createUserEvent(String tittle, String description, Long orderId, LocalDateTime dateTime, UserEventPriority priority) {
        return new UserEvent(
            orderId,
            tittle,
            description,
            dateTime.format(outputDateFormat),
            dateTime.format(outputTimeFormat),
            dateTime.format(outputDateTimeFormat),
            dateTime,
            priority
        );
    }

    @SneakyThrows
    private LocalDateTime parseAdditionalDateString(String additionalDateString) {
        if (StringUtils.isBlank(additionalDateString)) {
            return LocalDateTime.now();
        }
        additionalDateString = additionalDateString.replace("T", " ");
        return Timestamp.from(inputDateFormat.parse(additionalDateString).toInstant()).toLocalDateTime();
    }

    @Service
    @AllArgsConstructor
    public static class CalendarUserEventsTransformer {

        public List<JsonCalendarDto> toJsonEvents(List<UserEvent> userEvents) {
            return userEvents.stream().map(e -> new JsonCalendarDto(
                e.tittle,
                e.dateTime,
                e.description,
                getTextColor(e.priority),
                getBorderColor(e.priority),
                getBackgroundColor(e.priority),
                createViewOrderUrl(e.orderId)
            )).toList();
        }

        private String getTextColor(UserEventPriority priority) {
            return switch (priority) {
                case HIGH -> "white";
                case MEDIUM -> "black";
                case LOW -> "white";
            };
        }

        private String getBorderColor(UserEventPriority priority) {
            return switch (priority) {
                case HIGH -> "black";
                case MEDIUM -> "black";
                case LOW -> "black";
            };
        }

        private String getBackgroundColor(UserEventPriority priority) {
            return switch (priority) {
                case HIGH -> "#da1e28";
                case MEDIUM -> "#f1c21b";
                case LOW -> "#198038";
            };
        }

        private String createViewOrderUrl(Long orderId) {
            return "/order-redirect-by-authority?orderId=" + orderId;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record JsonCalendarDto(String title,
                                      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
                                      LocalDateTime start,
                                      String description,
                                      String textColor,
                                      String borderColor,
                                      String backgroundColor,
                                      String url) {
            @JsonProperty("allDay")
            public boolean allDay() {
                return true;
            }
        }
    }

    public enum UserEventPriority {
        HIGH,
        MEDIUM,
        LOW
    }

    public record UserEvent(Long orderId,
                            String tittle,
                            String description,
                            String dateString,
                            String timeString,
                            String dateTimeString,
                            LocalDateTime dateTime,
                            UserEventPriority priority) {

    }
}
