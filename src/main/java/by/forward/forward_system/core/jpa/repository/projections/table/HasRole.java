package by.forward.forward_system.core.jpa.repository.projections.table;

import by.forward.forward_system.core.enums.auth.Authority;

import java.util.List;

public interface HasRole {
    List<String> getRoles();

    List<String> getRolesRus();

    void addAuthority(Authority authority);
}
