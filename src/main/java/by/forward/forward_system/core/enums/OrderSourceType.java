package by.forward.forward_system.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderSourceType {
    M("M"),
    D("D"),
    F("F"),
    G("G"),
    X("X"),
    SEV("SEV"),
    VseSdal("Всё Сдал"),
    LigaZnaniy("Лига Знаний"),
    Lichniy("Личный (NR)"),
    VivodA24("Вывод А24 (NR)"),
    TgShpionViktoria("TgШпион (Виктория С.)"),
    TgShpionAnna("TgШпион (Анна М.)"),
    Unknown("Не указан"),
    ;
    private String rusName;
}
