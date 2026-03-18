package by.forward.forward_system.core.iternalnotification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InformationLevel {
    INFO("ℹ️"),
    WARN("⚠️"),
    DANGER("🚨"),
    ;

    private final String icon;
}
