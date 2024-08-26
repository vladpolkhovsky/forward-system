package by.forward.forward_system.core.enums;

import lombok.Getter;

@Getter
public enum DisciplineQualityType {
    EXCELLENT("EXCELLENT", "Отличное"),
    GOOD("GOOD", "Хорошее"),
    MAYBE("MAYBE", "Может рассмотреть"),
    ;

    private final String name;

    private final String rusName;

    DisciplineQualityType(String name, String rusName) {
        this.name = name;
        this.rusName = rusName;
    }

    public static DisciplineQualityType byName(String name) {
        for (DisciplineQualityType status : DisciplineQualityType.values()) {
            if (status.name.equals(name)) {
                return status;
            }
        }
        return null;
    }
}
