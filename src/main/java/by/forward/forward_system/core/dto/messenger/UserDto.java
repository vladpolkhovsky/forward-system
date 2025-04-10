package by.forward.forward_system.core.dto.messenger;

import lombok.*;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String fio;
    private String username;
    private Boolean isAdmin;
    private String roles;
    private String rolesRus;
}
