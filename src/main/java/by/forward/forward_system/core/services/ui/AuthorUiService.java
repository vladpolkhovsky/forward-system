package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.AuthorUiDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.AuthorService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorUiService {

    private final AuthorService authorService;

    private final UserRepository userRepository;

    public AuthorUiDto getAuthor(Long id) {
        Optional<AuthorEntity> byId = authorService.getById(id);
        AuthorEntity authorEntity = byId.orElseThrow(() -> new UsernameNotFoundException("Author not found with id " + id));
        return toDto(authorEntity);
    }

    public List<AuthorUiDto> getAllAuthors() {
        return authorService.getAllAuthors().stream()
            .map(this::toDto)
            .toList();
    }

    public AuthorUiDto createAuthor(AuthorUiDto user) {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();
        Optional<UserEntity> byUsername = userRepository.findByUsername(currentUserDetails.getUsername());
        UserEntity createdBy = byUsername.orElseThrow(() -> new UsernameNotFoundException("User not found with username " + currentUserDetails.getUsername()));

        AuthorEntity entity = toEntity(user);
        AuthorEntity save = authorService.save(createdBy, entity);

        return toDto(save);
    }


    public AuthorUiDto updateAuthor(Long id, AuthorUiDto user) {
        AuthorEntity entity = toEntity(user);
        authorService.update(id, entity);
        return toDto(entity);
    }


    private AuthorUiDto toDto(AuthorEntity authorEntity) {
        String fioFull = authorEntity.getUser().getLastname() + " " + authorEntity.getUser().getFirstname() + " " + authorEntity.getUser().getSurname();
        return new AuthorUiDto(
            authorEntity.getId(),
            authorEntity.getUser().getUsername(),
            authorEntity.getUser().getPassword(),
            authorEntity.getUser().getFio(),
            fioFull,
            authorEntity.getUser().getFirstname(),
            authorEntity.getUser().getLastname(),
            authorEntity.getUser().getSurname(),
            authorEntity.getUser().getContact(),
            authorEntity.getUser().getContactTelegram(),
            authorEntity.getUser().getEmail(),
            authorEntity.getUser().getPayment(),
            authorEntity.getUser().getOther(),
            authorEntity.getSubjects(),
            authorEntity.getQuality(),
            authorEntity.getUser().getAuthorities().contains(Authority.ADMIN),
            authorEntity.getUser().getAuthorities().contains(Authority.MANAGER),
            authorEntity.getUser().getAuthorities().contains(Authority.AUTHOR),
            authorEntity.getUser().getAuthorities().contains(Authority.HR),
            authorEntity.getUser().getAuthorities().contains(Authority.OWNER)
        );
    }

    private AuthorEntity toEntity(AuthorUiDto authorUiDto) {
        AuthorEntity authorEntity = new AuthorEntity();

        authorEntity.setId(authorUiDto.getId());
        authorEntity.setQuality(authorUiDto.getQuality());
        authorEntity.setSubjects(authorUiDto.getSubject());

        UserEntity userEntity = new UserEntity();

        userEntity.setId(authorUiDto.getId());
        userEntity.setUsername(authorUiDto.getUsername());
        userEntity.setPassword(authorUiDto.getPassword());
        userEntity.setLastname(authorUiDto.getLastname());
        userEntity.setFirstname(authorUiDto.getFirstname());
        userEntity.setSurname(authorUiDto.getSurname());
        userEntity.setContact(authorUiDto.getContact());
        userEntity.setEmail(authorUiDto.getEmail());
        userEntity.setOther(authorUiDto.getOther());
        userEntity.setContactTelegram(authorUiDto.getContactTelegram());
        userEntity.setPayment(authorUiDto.getPayment());
        userEntity.setRoles("");

        authorEntity.setUser(userEntity);

        userEntity.addRole(Authority.AUTHOR);

        return authorEntity;
    }
}
