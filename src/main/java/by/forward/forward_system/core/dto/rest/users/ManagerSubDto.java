package by.forward.forward_system.core.dto.rest.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerSubDto {
    private UserDto subManager;
    private boolean hasSubManager;
}
