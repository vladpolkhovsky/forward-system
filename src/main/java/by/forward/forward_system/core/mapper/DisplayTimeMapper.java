package by.forward.forward_system.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {
    LocalDateTime.class
})
public interface DisplayTimeMapper {

    DateTimeFormatter lessThen12hours = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter moreThen12hours = DateTimeFormatter.ofPattern("dd.MM' в 'HH:mm");
    DateTimeFormatter yearChanged = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Named("toDisplayableString")
    default String toDisplayableString(LocalDateTime chatLastMessageTime) {
        if (chatLastMessageTime == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.minusMinutes(1).isBefore(chatLastMessageTime)) {
            return "Только что";
        }
        if (now.minusMinutes(2).isBefore(chatLastMessageTime)) {
            return "Минуту назад";
        }
        if (now.minusMinutes(5).isBefore(chatLastMessageTime)) {
            return "Недавно";
        }
        if (now.minusMinutes(55).isBefore(chatLastMessageTime)) {
            return Math.abs(Duration.between(now, chatLastMessageTime).toMinutes()) + " мин. назад";
        }
        if (now.minusHours(12).isBefore(chatLastMessageTime)) {
            return chatLastMessageTime.format(lessThen12hours);
        }
        if (chatLastMessageTime.getYear() == now.getYear()) {
            return chatLastMessageTime.format(moreThen12hours);
        }
        return chatLastMessageTime.format(yearChanged);
    }
}
