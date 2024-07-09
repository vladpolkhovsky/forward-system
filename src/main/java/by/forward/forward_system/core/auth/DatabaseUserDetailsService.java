package by.forward.forward_system.core.auth;

import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        UserEntity userEntity = byUsername.orElseThrow(() -> new UsernameNotFoundException(username));
        return new DatabaseUserDetails(userEntity);
    }
}
