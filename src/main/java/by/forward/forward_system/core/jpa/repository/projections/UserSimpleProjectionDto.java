package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class UserSimpleProjectionDto {

    private Long id;
    private String username;
    private List<String> roles;
    private List<String> rolesRus;

    public UserSimpleProjectionDto(UserEntity user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.roles = user.getAuthorities().stream().map(Authority::getAuthority).toList();
        this.rolesRus = user.getAuthorities().stream().map(Authority::getAuthorityNameRus).toList();
    }
}
