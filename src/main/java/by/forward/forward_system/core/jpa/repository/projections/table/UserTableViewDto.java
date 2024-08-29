package by.forward.forward_system.core.jpa.repository.projections.table;

import by.forward.forward_system.core.enums.auth.Authority;

import java.util.ArrayList;
import java.util.List;

public class UserTableViewDto implements Named, OrderParticipant, HasRole {

    private final List<String> orderTechNumbers = new ArrayList<>();

    private final List<Authority> authorities = new ArrayList<>();

    private final String username;

    public UserTableViewDto(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<String> orderTechNumbers() {
        return orderTechNumbers;
    }

    @Override
    public int activeOrderCount() {
        return orderTechNumbers.size();
    }

    @Override
    public List<String> getRoles() {
        return authorities.stream().map(Authority::getAuthority).toList();
    }

    @Override
    public List<String> getRolesRus() {
        return authorities.stream().map(Authority::getAuthorityNameRus).sorted(String::compareTo).toList();
    }

    @Override
    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }
}
