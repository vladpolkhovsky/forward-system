package by.forward.forward_system.core.dto.rest.users;

import by.forward.forward_system.core.enums.auth.Authority;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String middleName;
    private List<Authority> authorities;
}
