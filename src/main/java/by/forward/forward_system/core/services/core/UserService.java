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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Optional<UserEntity> getById(Long id) {
        return userRepository.findById(id);
    }

    public UserDto getConverted(Long id) {
        return convertUserToDto(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

    public Optional<UserEntity> getByUsername(String username) {
        return userRepository.findByUsernameAndDeletedIsFalse(username);
    }

    public UserEntity save(UserEntity user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.saveAndFlush(user);
    }

    public UserEntity update(Long id, UserEntity user) {
        Optional<UserEntity> byId = userRepository.findById(id);
        UserEntity userEntity = byId.orElseThrow(() -> new RuntimeException("User with id " + id + " not found"));

        if (!StringUtils.isEmpty(user.getPassword())) {
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        Set<Authority> authorities = userEntity.getAuthorities().stream().collect(Collectors.toSet());
        authorities.addAll(user.getAuthorities());

        userEntity.setFirstname(user.getFirstname());
        userEntity.setLastname(user.getLastname());
        userEntity.setSurname(user.getSurname());
        userEntity.setUsername(user.getUsername());
        userEntity.setContact(user.getContact());
        userEntity.setEmail(user.getEmail());
        userEntity.setOther(user.getOther());
        userEntity.setDeleted(user.getDeleted());

        userEntity.addRoles(authorities);

        return userRepository.save(userEntity);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findByRolesNotContainsAndDeletedIsFalse(Authority.AUTHOR.getAuthority());
    }

    public List<UserEntity> getAllUserWithoutRole(Authority authority) {
        return userRepository.findByRolesNotContainsAndDeletedIsFalse(authority.getAuthority());
    }

    public List<UserEntity> findUsersWithRole(String role) {
        return userRepository.findByRolesContainsAndDeletedIsFalse(role);
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
        userDto.setRoles(userEntity.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.joining(", ")));
        userDto.setRolesRus(userEntity.getAuthorities().stream().map(Authority::getAuthorityNameRus).collect(Collectors.joining(", ")));
        return userDto;
    }

    public Collection<UserEntity> getAllUsersFast() {
        return userRepository.getUsersFast();
    }

}
