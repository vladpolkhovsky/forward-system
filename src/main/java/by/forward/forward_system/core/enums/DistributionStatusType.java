package by.forward.forward_system.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributionStatusType {
    IN_PROGRESS("В процессе"),
    ENDED("Завершен"),
    ENDED_BY_MANAGER("Завершен принудительно");

    private final String rusName;
}
