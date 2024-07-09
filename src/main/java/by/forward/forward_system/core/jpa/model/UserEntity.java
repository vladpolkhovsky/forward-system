package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.auth.Authority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "forward_system")
public class UserEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "fio", nullable = false, length = 255)
    private String fio;

    @Column(name = "roles", nullable = false, length = 512)
    private String roles;

    @Column(name = "contact", nullable = false, length = 512)
    private String contact;

    @Column(name = "payment", nullable = false, length = 255)
    private String payment;

    @Column(name = "contactTelegram", nullable = false, length = 512)
    private String contactTelegram;

    @Column(name = "email", nullable = false, length = 512)
    private String email;

    @Column(name = "other", length = 2048)
    private String other;

    public void addRole(Authority grantedAuthority) {
        List<Authority> authorities = getAuthorities();
        if (!authorities.contains(grantedAuthority)) {
            authorities.add(grantedAuthority);
        }
        setRoles(toRoles(authorities));
    }

    public void removeAuthority(Authority grantedAuthority) {
        List<Authority> authorities = getAuthorities();
        authorities.remove(grantedAuthority);
        setRoles(toRoles(authorities));
    }

    public List<Authority> getAuthorities() {
        return new ArrayList<>(Arrays.stream(Optional.ofNullable(getRoles()).orElse("").split(","))
            .filter(StringUtils::isNotBlank)
            .map(Authority::byName)
            .toList());
    }

    private String toRoles(List<Authority> authorities) {
        return String.join(",", authorities.stream().map(Authority::getAuthority).toList());
    }

}