package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.services.NewUserService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/user")
@AllArgsConstructor
public class UserRestController {

    private final NewUserService newUserService;

    @GetMapping("/info")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto userById = newUserService.getUserById(AuthUtils.getCurrentUserId());
        return ResponseEntity.ok(userById);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userById = newUserService.getUserById(id);
        return ResponseEntity.ok(userById);
    }
}
