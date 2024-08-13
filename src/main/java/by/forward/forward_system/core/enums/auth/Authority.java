package by.forward.forward_system.core.enums.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Optional;

public enum Authority implements GrantedAuthority {

    OWNER("OWNER"),
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    HR("HR"),
    AUTHOR("AUTHOR"),
    BANNED("BANNED"),

    ;

    private final String authorityName;

    Authority(String authorityName) {
        this.authorityName = authorityName;
    }

    public static Authority byName(String name) {
        Optional<Authority> any = Arrays.stream(Authority.values()).filter(t -> t.authorityName.equalsIgnoreCase(name)).findAny();
        return any.orElse(null);
    }

    @Override
    public String getAuthority() {
        return authorityName;
    }

}
