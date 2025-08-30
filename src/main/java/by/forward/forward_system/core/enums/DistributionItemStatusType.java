package by.forward.forward_system.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributionItemStatusType {
    WAITING("Ожидает отправки"),
    IN_PROGRESS("Ожидаем ответа"),
    NO_RESPONSE("Нет ответа"),
    SKIPPED("Пропущен"),
    DECLINE("Отказ"),
    TALK("Обсуждение"),
    ACCEPTED("Принял");

    private final String rusName;
}
