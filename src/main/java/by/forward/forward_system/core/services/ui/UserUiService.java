package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.UserShortUiDto;
import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.services.core.ChatUtilsService;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserUiService {

    private final UserService userService;

    private final ChatUtilsService chatUtilsService;

    public UserShortUiDto getCurrentUser() {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();
        Optional<UserEntity> byUsername = userService.getByUsername(currentUserDetails.getUsername());

        UserEntity userEntity = byUsername
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + currentUserDetails.getUsername()));

        return new UserShortUiDto(
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getFioFull(),
            userEntity.getAuthorities().stream().map(Authority::getAuthorityNameRus).collect(Collectors.joining(", "))
        );
    }

    public void checkAccessAdmin() {
        UserEntity userEntity = userService.getById(getCurrentUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found with id " + getCurrentUserId()));
        if (userEntity.getAuthorities().contains(Authority.ADMIN) || userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }
        throw new IllegalStateException("Нет доступа к просмотру страницы.");
    }

    public void checkAccessManager() {
        UserEntity userEntity = userService.getById(getCurrentUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found with id " + getCurrentUserId()));
        if (userEntity.getAuthorities().contains(Authority.MANAGER) || userEntity.getAuthorities().contains(Authority.ADMIN) || userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }
        throw new IllegalStateException("Нет доступа к просмотру страницы.");
    }

    public void checkAccessHR() {
        UserEntity userEntity = userService.getById(getCurrentUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found with id " + getCurrentUserId()));
        if (userEntity.getAuthorities().contains(Authority.HR) || userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }
        throw new IllegalStateException("Нет доступа к просмотру страницы.");
    }

    public void checkAccessOwner() {
        UserEntity userEntity = userService.getById(getCurrentUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found with id " + getCurrentUserId()));
        if (userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }
        throw new IllegalStateException("Нет доступа к просмотру страницы.");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Boolean isCurrentUserAdmin() {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();
        Optional<UserEntity> byUsername = userService.getByUsername(currentUserDetails.getUsername());
        return byUsername.get().getAuthorities().contains(Authority.ADMIN);
    }

    public Boolean isCurrentUserOwner() {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();
        Optional<UserEntity> byUsername = userService.getByUsername(currentUserDetails.getUsername());
        return byUsername.get().getAuthorities().contains(Authority.OWNER);
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

        if (userUiDto.getIsAdmin()) {
            chatUtilsService.addAdminToAllChats(save.getId());
        }

        if (userUiDto.getIsManager()) {
            chatUtilsService.createNewOrderChats(save.getId());
        }

        chatUtilsService.addToNewsChat(save.getId());

        return toDto(save);
    }

    public UserUiDto updateUser(Long id, UserUiDto userUiDto) {
        UserEntity user = toEntity(userUiDto);
        UserEntity save = userService.update(id, user);
        UserUiDto dto = toDto(save);

        if (dto.getIsAdmin()) {
            chatUtilsService.addAdminToAllChats(save.getId());
        }

        if (userUiDto.getIsManager()) {
            chatUtilsService.createNewOrderChats(save.getId());
        }

        chatUtilsService.addToNewsChat(save.getId());

        return dto;
    }

    public List<UserUiDto> findOnlyRole(Authority authority) {
        return userService.findUsersWithRole(authority.getAuthority())
            .stream()
            .map(this::toDto)
            .toList();
    }

    private UserUiDto toDto(UserEntity userEntity) {
        String fioFull = userEntity.getLastname() + " " + userEntity.getFirstname() + " " + userEntity.getSurname();
        return new UserUiDto(
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getPassword(),
            userEntity.getFirstname(),
            userEntity.getLastname(),
            userEntity.getSurname(),
            userEntity.getFio(),
            fioFull,
            userEntity.getContact(),
            userEntity.getContactTelegram(),
            userEntity.getEmail(),
            userEntity.getPayment(),
            userEntity.getOther(),
            userEntity.getAuthorities().contains(Authority.ADMIN),
            userEntity.getAuthorities().contains(Authority.MANAGER),
            userEntity.getAuthorities().contains(Authority.AUTHOR),
            userEntity.getAuthorities().contains(Authority.HR),
            userEntity.getAuthorities().contains(Authority.OWNER),
            userEntity.getAuthorities().stream().map(Authority::getAuthorityNameRus).collect(Collectors.joining(", "))
        );
    }

    private UserEntity toEntity(UserUiDto userUiDto) {
        UserEntity userEntity = new UserEntity();

        userEntity.setId(userUiDto.getId());
        userEntity.setUsername(userUiDto.getUsername());
        userEntity.setPassword(userUiDto.getPassword());
        userEntity.setLastname(userUiDto.getLastname());
        userEntity.setFirstname(userUiDto.getFirstname());
        userEntity.setSurname(userUiDto.getSurname());
        userEntity.setContact(userUiDto.getContact());
        userEntity.setEmail(userUiDto.getEmail());
        userEntity.setOther(userUiDto.getOther());
        userEntity.setContactTelegram(userUiDto.getContactTelegram());
        userEntity.setPayment(userUiDto.getPayment());
        userEntity.setRoles("");

        if (userUiDto.getIsAuthor() != null && userUiDto.getIsAuthor()) {
            userEntity.addRole(Authority.AUTHOR);
        }

        if (userUiDto.getIsAdmin() != null && userUiDto.getIsAdmin()) {
            userEntity.addRole(Authority.ADMIN);
        }

        if (userUiDto.getIsManager() != null && userUiDto.getIsManager()) {
            userEntity.addRole(Authority.MANAGER);
        }

        if (userUiDto.getIsHr() != null && userUiDto.getIsHr()) {
            userEntity.addRole(Authority.HR);
        }

        if (userUiDto.getIsOwner() != null && userUiDto.getIsOwner()) {
            userEntity.addRole(Authority.OWNER);
        }

        return userEntity;
    }

}
