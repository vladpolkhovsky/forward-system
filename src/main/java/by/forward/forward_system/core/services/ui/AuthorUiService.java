package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.AuthorUiDto;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.DisciplineQualityRepository;
import by.forward.forward_system.core.jpa.repository.DisciplineRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.AuthorService;
import by.forward.forward_system.core.utils.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthorUiService {

    private final AuthorService authorService;

    private final UserRepository userRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineQualityRepository disciplineQualityRepository;

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

    public List<AuthorUiDto> getAllAuthorsFast() {
        return authorService.getAllAuthorsFast().stream()
            .map(this::toDtoWithoutDisciplines)
            .sorted(Comparator.comparing(AuthorUiDto::getUsername))
            .toList();
    }

    @Transactional
    public AuthorUiDto createAuthor(AuthorUiDto user) {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();

        Optional<UserEntity> byUsername = userRepository.findByUsername(currentUserDetails.getUsername());
        UserEntity createdBy = byUsername.orElseThrow(() -> new UsernameNotFoundException("User not found with username " + currentUserDetails.getUsername()));

        AuthorEntity entity = toEntity(user);
        AuthorEntity save = authorService.save(createdBy, entity);

        return toDto(save);
    }

    @Transactional
    public AuthorUiDto updateAuthor(Long id, AuthorUiDto user) {
        AuthorEntity entity = toEntity(user);
        authorService.update(id, entity);
        return toDto(entity);
    }

    private AuthorUiDto toDtoWithoutDisciplines(AuthorEntity authorEntity) {
        String fioFull = authorEntity.getUser().getLastname() + " " +
            authorEntity.getUser().getFirstname() + " " +
            StringUtils.defaultIfEmpty(authorEntity.getUser().getSurname(), "");

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
            null,
            null,
            null,
            authorEntity.getUser().getAuthorities().contains(Authority.ADMIN),
            authorEntity.getUser().getAuthorities().contains(Authority.MANAGER),
            authorEntity.getUser().getAuthorities().contains(Authority.AUTHOR),
            authorEntity.getUser().getAuthorities().contains(Authority.HR),
            authorEntity.getUser().getAuthorities().contains(Authority.OWNER),
            authorEntity.getUser().getAuthorities().stream().map(Authority::getAuthorityNameRus).collect(Collectors.joining(", "))
        );
    }

    private AuthorUiDto toDto(AuthorEntity authorEntity) {
        String fioFull = authorEntity.getUser().getLastname() + " " +
            authorEntity.getUser().getFirstname() + " " +
            StringUtils.defaultIfEmpty(authorEntity.getUser().getSurname(), "");

        String excellentSubjects = getDisciplineIds(authorEntity.getAuthorDisciplines(), DisciplineQualityType.EXCELLENT);
        String goodSubjects = getDisciplineIds(authorEntity.getAuthorDisciplines(), DisciplineQualityType.GOOD);
        String maybeSubjects = getDisciplineIds(authorEntity.getAuthorDisciplines(), DisciplineQualityType.MAYBE);

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
            excellentSubjects,
            goodSubjects,
            maybeSubjects,
            authorEntity.getUser().getAuthorities().contains(Authority.ADMIN),
            authorEntity.getUser().getAuthorities().contains(Authority.MANAGER),
            authorEntity.getUser().getAuthorities().contains(Authority.AUTHOR),
            authorEntity.getUser().getAuthorities().contains(Authority.HR),
            authorEntity.getUser().getAuthorities().contains(Authority.OWNER),
            authorEntity.getUser().getAuthorities().stream().map(Authority::getAuthorityNameRus).collect(Collectors.joining(", "))
        );
    }

    private String getDisciplineIds(List<AuthorDisciplineEntity> authorDisciplines, DisciplineQualityType disciplineQualityType) {
        List<Long> ids = new ArrayList<>();
        for (AuthorDisciplineEntity authorDiscipline : authorDisciplines) {
            if (authorDiscipline.getDisciplineQuality().getType().equals(disciplineQualityType)) {
                ids.add(authorDiscipline.getDiscipline().getId());
            }
        }
        return ids.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    private AuthorEntity toEntity(AuthorUiDto authorUiDto) {
        AuthorEntity authorEntity = new AuthorEntity();

        authorEntity.setId(authorUiDto.getId());

        authorEntity.getAuthorDisciplines().addAll(createSubjects(authorEntity, authorUiDto.getExcellentSubjects(), DisciplineQualityType.EXCELLENT));
        authorEntity.getAuthorDisciplines().addAll(createSubjects(authorEntity, authorUiDto.getGoodSubjects(), DisciplineQualityType.GOOD));
        authorEntity.getAuthorDisciplines().addAll(createSubjects(authorEntity, authorUiDto.getMaybeSubjects(), DisciplineQualityType.MAYBE));

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

    private List<AuthorDisciplineEntity> createSubjects(AuthorEntity authorEntity, String excellentSubjects, DisciplineQualityType disciplineQualityType) {
        DisciplineQualityEntity disciplineQuality = disciplineQualityRepository.findById(disciplineQualityType.getName()).orElseThrow(() -> new RuntimeException("disciplineQualityType not found"));

        List<Long> disciplineIds = Arrays.stream(excellentSubjects.split(",")).filter(StringUtils::isNotBlank).map(Long::parseLong).toList();
        List<DisciplineEntity> allById = disciplineRepository.findAllById(disciplineIds);

        List<AuthorDisciplineEntity> authorDisciplineEntities = new ArrayList<>();
        for (DisciplineEntity discipline : allById) {
            AuthorDisciplineEntity authorDisciplineEntity = new AuthorDisciplineEntity();
            authorDisciplineEntity.setAuthor(authorEntity);
            authorDisciplineEntity.setDisciplineQuality(disciplineQuality);
            authorDisciplineEntity.setDiscipline(discipline);
            authorDisciplineEntities.add(authorDisciplineEntity);
        }

        return authorDisciplineEntities;
    }
}
