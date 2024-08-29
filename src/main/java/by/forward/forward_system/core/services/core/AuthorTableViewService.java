package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.table.AuthorTableViewDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@AllArgsConstructor
public class AuthorTableViewService {

    private final AuthorRepository authorRepository;

    private final OrderRepository orderRepository;

    public List<AuthorTableViewDto> get(String selectedDiscipline, String col, String order) {
        boolean isAllDisciplines = selectedDiscipline.equals("all");
        Predicate<AuthorEntity> filterDiscipline = (t) -> true;

        if (!isAllDisciplines) {
            Long disciplineId = Long.parseLong(selectedDiscipline);
            filterDiscipline = (t) -> {
                Optional<AuthorDisciplineEntity> any = t.getAuthorDisciplines().stream().filter(d -> d.getDiscipline().getId().equals(disciplineId)).findAny();
                return any.isPresent();
            };
        }

        Comparator<AuthorTableViewDto> comparator = Comparator.comparing((a) -> a.getUsername().toLowerCase());
        if (col.equals("activeOrders")) {
            comparator = Comparator.comparing(AuthorTableViewDto::activeOrderCount);
        }

        if (order.equals("desc")) {
            comparator = comparator.reversed();
        }

        return authorRepository.findAll().stream()
            .filter(filterDiscipline)
            .map(this::toViewDto)
            .sorted(comparator)
            .toList();
    }

    private AuthorTableViewDto toViewDto(AuthorEntity authorEntity) {
        String username = authorEntity.getUser().getUsername();
        AuthorTableViewDto authorTableViewDto = new AuthorTableViewDto(username);

        for (AuthorDisciplineEntity authorDiscipline : authorEntity.getAuthorDisciplines()) {
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.EXCELLENT)) {
                authorTableViewDto.excellent().add(authorDiscipline.getDiscipline().getName());
            }
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.GOOD)) {
                authorTableViewDto.good().add(authorDiscipline.getDiscipline().getName());
            }
            if (authorDiscipline.getDisciplineQuality().getType().equals(DisciplineQualityType.MAYBE)) {
                authorTableViewDto.maybe().add(authorDiscipline.getDiscipline().getName());
            }
        }

        List<String> allTechNumberByParticipant = orderRepository.getAllTechNumberByParticipant(
            authorEntity.getId(),
            Arrays.asList(ParticipantType.MAIN_AUTHOR.getName(), ParticipantType.EXPERT.getName()),
            Arrays.asList(OrderStatus.CLOSED.getName())
        );

        authorTableViewDto.orderTechNumbers().addAll(allTechNumberByParticipant);

        authorTableViewDto.orderTechNumbers().sort(String::compareTo);
        authorTableViewDto.excellent().sort(String::compareTo);
        authorTableViewDto.good().sort(String::compareTo);
        authorTableViewDto.maybe().sort(String::compareTo);

        return authorTableViewDto;
    }

}
