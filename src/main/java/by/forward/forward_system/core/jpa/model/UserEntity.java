package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.auth.Authority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "forward_system")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "firstname", nullable = false, length = 255)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 255)
    private String lastname;

    @Column(name = "surname")
    private String surname;

    @Column(name = "roles", nullable = false, length = 512)
    private String roles;

    @Column(name = "contact", nullable = false, length = 512)
    private String contact;

    @Column(name = "payment", length = 255)
    private String payment;

    @Column(name = "contact_telegram", nullable = false, length = 512)
    private String contactTelegram;

    @Column(name = "email", length = 512)
    private String email;

    @Column(name = "other", length = 2048)
    private String other;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public void addRole(Authority grantedAuthority) {
        List<Authority> authorities = getAuthorities();
        if (!authorities.contains(grantedAuthority)) {
            authorities.add(grantedAuthority);
        }
        setRoles(toRoles(authorities));
    }

    public void addRoles(Set<Authority> grantedAuthorities) {
        grantedAuthorities.forEach(this::addRole);
    }

    public void removeRole(Authority grantedAuthority) {
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

    public String getFio() {
        return getFirstname() + " " +
               getLastname().substring(0, 1) + ". " +
               StringUtils.abbreviate(StringUtils.defaultString(getSurname(), ""), ".", 2);
    }

    public String getFioFull() {
        return getLastname() + " " + getFirstname() + " " + getSurname();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}