package by.forward.forward_system.core.enums.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Optional;

public enum Authority implements GrantedAuthority {

    OWNER("OWNER", "Владелец"),
    ADMIN("ADMIN", "Администратор"),
    MANAGER("MANAGER", "Менеджер"),
    HR("HR", "HR"),
    AUTHOR("AUTHOR", "Автор"),
    BANNED("BANNED", "Заблокирован"),

    ;

    private final String authorityName;
    private final String authorityNameRus;

    Authority(String authorityName, String authorityNameRus) {
        this.authorityName = authorityName;
        this.authorityNameRus = authorityNameRus;
    }

    public static Authority byName(String name) {
        Optional<Authority> any = Arrays.stream(Authority.values()).filter(t -> t.authorityName.equalsIgnoreCase(name)).findAny();
        return any.orElse(null);
    }

    @Override
    public String getAuthority() {
        return authorityName;
    }

    public String getAuthorityNameRus() {
        return authorityNameRus;
    }

}
