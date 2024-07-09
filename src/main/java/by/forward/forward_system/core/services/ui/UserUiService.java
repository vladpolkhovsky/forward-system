package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.UserShortUiDto;
import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserUiService {

    private final UserService userService;

    public UserShortUiDto getCurrentUser() {

        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();
        Optional<UserEntity> byUsername = userService.getByUsername(currentUserDetails.getUsername());

        UserEntity userEntity = byUsername
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + currentUserDetails.getUsername()));

        return new UserShortUiDto(
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getFio()
        );
    }

    public List<UserUiDto> getAllUsers() {
        return userService.getAllUsers().stream()
            .map(this::toDto)
            .toList();
    }

    public UserUiDto getUser(Long id) {
        Optional<UserEntity> byId = userService.getById(id);
        UserEntity userEntity = byId.orElseThrow(() -> new UsernameNotFoundException("User not found with id " + id));
        return toDto(userEntity);
    }

    public UserUiDto createUser(UserUiDto userUiDto) {
        UserEntity user = toEntity(userUiDto);

        UserEntity save = userService.save(user);

        return toDto(save);
    }

    public UserUiDto updateUser(Long id, UserUiDto userUiDto) {
        UserEntity user = toEntity(userUiDto);

        UserEntity save = userService.update(id, user);

        return toDto(save);
    }

    private UserUiDto toDto(UserEntity userEntity) {
        return new UserUiDto(
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getPassword(),
            userEntity.getFio(),
            userEntity.getContact(),
            userEntity.getContactTelegram(),
            userEntity.getEmail(),
            userEntity.getPayment(),
            userEntity.getOther(),
            userEntity.getAuthorities().contains(Authority.ADMIN),
            userEntity.getAuthorities().contains(Authority.MANAGER),
            userEntity.getAuthorities().contains(Authority.AUTHOR),
            userEntity.getAuthorities().contains(Authority.HR),
            userEntity.getAuthorities().contains(Authority.OWNER)
        );
    }

    private UserEntity toEntity(UserUiDto userUiDto) {
        UserEntity userEntity = new UserEntity();

        userEntity.setId(userUiDto.id());
        userEntity.setUsername(userUiDto.username());
        userEntity.setPassword(userUiDto.password());
        userEntity.setFio(userUiDto.fio());
        userEntity.setContact(userUiDto.contact());
        userEntity.setEmail(userUiDto.email());
        userEntity.setOther(userUiDto.other());
        userEntity.setContactTelegram(userEntity.getContactTelegram());
        userEntity.setPayment(userEntity.getPayment());
        userEntity.setRoles("");

        if (userUiDto.isAuthor() != null && userUiDto.isAuthor()) {
            userEntity.addRole(Authority.AUTHOR);
        }

        if (userUiDto.isAdmin() != null && userUiDto.isAdmin()) {
            userEntity.addRole(Authority.ADMIN);
        }

        return userEntity;
    }
}
