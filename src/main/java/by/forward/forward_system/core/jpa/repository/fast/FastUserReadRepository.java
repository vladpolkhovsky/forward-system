package by.forward.forward_system.core.jpa.repository.fast;

import by.forward.forward_system.core.enums.auth.Authority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FastUserReadRepository {

    @Language("SQL")
    private static final String LOAD_ALL_ACTIVE_USERS_SQL = """
            select distinct u.id as id, u.username as username, u.roles as roles from forward_system.users u where not u.is_deleted
        """;
    private final JdbcTemplate jdbcTemplate;

    public List<UserDto> readAllActiveUsers() {
        return jdbcTemplate.query(LOAD_ALL_ACTIVE_USERS_SQL, (rs, rowNum) -> new UserDto(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("roles")
        ));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String roles;

        public boolean is(Authority authority) {
            return roles.contains(authority.getAuthority());
        }
    }
}
