package by.forward.forward_system.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributionStatusType {
    PLANNED("Запланировано"),
    IN_PROGRESS("В процессе"),
    ENDED("Завершено"),
    ENDED_BY_MANAGER("Завершено принудительно");

    private final String rusName;
}
