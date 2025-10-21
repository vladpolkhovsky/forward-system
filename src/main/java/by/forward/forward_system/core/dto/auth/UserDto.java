package by.forward.forward_system.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import by.forward.forward_system.core.enums.auth.Authority;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
public class UserDto implements UserDetails {
    private Long id;
    private String username;
    private List<Authority> roles;

    @JsonIgnore
    private String password;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    private String createdAt;
    private String updatedAt;
}
