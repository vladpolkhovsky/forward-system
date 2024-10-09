package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.repository.AuthorDisciplineRepository;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.DisciplineProjection;
import by.forward.forward_system.core.jpa.repository.projections.table.AuthorTableViewDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

@Service
@AllArgsConstructor
public class AuthorTableViewService {

    private final AuthorRepository authorRepository;

    private final OrderRepository orderRepository;

    private final AuthorDisciplineRepository authorDisciplineRepository;

    public List<AuthorTableViewDto> get(String selectedDiscipline, String col, String order) {
        boolean isAllDisciplines = selectedDiscipline.equals("all");
        Predicate<AuthorEntity> filterDiscipline = (t) -> true;

        List<DisciplineProjection> allDisciplines = authorDisciplineRepository.getAllDisciplines();
        HashMap<Long, Set<Long>> authorIdToDisciplinesSet = new HashMap<>();
        HashMap<Long, List<DisciplineProjection>> authorToDisciplines = new HashMap<>();

        for (DisciplineProjection allDiscipline : allDisciplines) {
            List<DisciplineProjection> allAuthorDisciplines = authorToDisciplines.getOrDefault(
                allDiscipline.getAuthorId(),
                new ArrayList<>()
            );
            Set<Long> authorDisciplinesSet = authorIdToDisciplinesSet.getOrDefault(
                allDiscipline.getAuthorId(),
                new HashSet<>()
            );

            authorDisciplinesSet.add(allDiscipline.getDisciplineId());
            allAuthorDisciplines.add(allDiscipline);

            authorIdToDisciplinesSet.put(allDiscipline.getAuthorId(), authorDisciplinesSet);
            authorToDisciplines.put(allDiscipline.getAuthorId(), allAuthorDisciplines);
        }

        if (!isAllDisciplines) {
            Long disciplineId = Long.parseLong(selectedDiscipline);
            filterDiscipline = (t) -> {
                Set<Long> disciplineIds = authorIdToDisciplinesSet.get(t.getId());
                return disciplineIds != null && disciplineIds.contains(disciplineId);
            };
        }

        Comparator<AuthorTableViewDto> comparator = Comparator.comparing((a) -> a.getUsername().toLowerCase());
        if (col.equals("activeOrders")) {
            comparator = Comparator.comparing(AuthorTableViewDto::activeOrderCount);
        }

        if (order.equals("desc")) {
            comparator = comparator.reversed();
        }

        return authorRepository.getAuthorsFast().stream()
            .filter(filterDiscipline)
            .map(authorEntity -> toViewDto(authorEntity, authorToDisciplines))
            .sorted(comparator)
            .toList();
    }

    private AuthorTableViewDto toViewDto(AuthorEntity authorEntity, HashMap<Long, List<DisciplineProjection>> authorToDisciplines) {
        String username = authorEntity.getUser().getUsername();
        AuthorTableViewDto authorTableViewDto = new AuthorTableViewDto(username);

        for (DisciplineProjection disciplineProjection : authorToDisciplines.getOrDefault(authorEntity.getId(), Collections.emptyList())) {
            if (disciplineProjection.getDisciplineQuality().equals(DisciplineQualityType.EXCELLENT.getName())) {
                authorTableViewDto.excellent().add(disciplineProjection.getDisciplineName());
            }
            if (disciplineProjection.getDisciplineQuality().equals(DisciplineQualityType.GOOD.getName())) {
                authorTableViewDto.good().add(disciplineProjection.getDisciplineName());
            }
            if (disciplineProjection.getDisciplineQuality().equals(DisciplineQualityType.MAYBE.getName())) {
                authorTableViewDto.maybe().add(disciplineProjection.getDisciplineName());
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
