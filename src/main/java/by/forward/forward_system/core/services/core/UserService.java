package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.UserDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Optional<UserEntity> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity save(UserEntity user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public UserEntity update(Long id, UserEntity user) {
        Optional<UserEntity> byId = userRepository.findById(id);
        UserEntity userEntity = byId.orElseThrow(() -> new RuntimeException("User with id " + id + " not found"));

        if (!StringUtils.isEmpty(user.getPassword())) {
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userEntity.setFirstname(user.getFirstname());
        userEntity.setLastname(user.getLastname());
        userEntity.setSurname(user.getSurname());
        userEntity.setUsername(user.getUsername());
        userEntity.setContact(user.getContact());
        userEntity.setEmail(user.getEmail());
        userEntity.setOther(user.getOther());
        userEntity.setRoles(user.getRoles());

        return userRepository.save(userEntity);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findByRolesNotContains(Authority.AUTHOR.getAuthority());
    }

    public List<UserEntity> findUsersWithRole(String role) {
        return userRepository.findByRolesContains(role);
    }

    public List<UserDto> getAllUsersConverted() {
        return userRepository.findAll().stream().map(this::convertUserToDto).toList();
    }

    public UserDto convertUserToDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setFio(userEntity.getFio());
        userDto.setUsername(userEntity.getUsername());
        userDto.setIsAdmin(userEntity.getAuthorities().contains(Authority.ADMIN));
        return userDto;
    }
}
