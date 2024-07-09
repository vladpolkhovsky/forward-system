package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public UserEntity update(Long id, UserEntity user) {
        Optional<UserEntity> byId = userRepository.findById(id);
        UserEntity userEntity = byId.orElseThrow(() -> new RuntimeException("User with id " + id + " not found"));

        if (!StringUtils.isEmpty(user.getPassword())) {
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userEntity.setFio(user.getFio());
        userEntity.setUsername(user.getUsername());
        userEntity.setContact(user.getContact());
        userEntity.setEmail(user.getEmail());
        userEntity.setOther(user.getOther());
        userEntity.setRoles(user.getRoles());

        return userRepository.save(userEntity);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
