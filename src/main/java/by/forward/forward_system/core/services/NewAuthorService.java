package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.DisciplineDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorDisciplinesDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorFullDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.DisciplineEntity;
import by.forward.forward_system.core.jpa.repository.AuthorRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository;
import by.forward.forward_system.core.jpa.repository.OrderRepository.ActiveOrderCountProjection;
import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import by.forward.forward_system.core.mapper.AuthorMapper;
import by.forward.forward_system.core.mapper.DisciplineMapper;
import by.forward.forward_system.core.services.core.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewAuthorService {

    private final OrderRepository orderRepository;

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    private final DisciplineMapper disciplineMapper;

    private final UserActivityService userActivityService;

    public AuthorDisciplinesDto getAuthorDisciplines(Long userId) {
        List<AuthorDisciplineEntity> disciplines = authorRepository.getAuthorByIdFetchedAll(userId)
            .getAuthorDisciplines();

        List<DisciplineDto> excellent = disciplines.stream()
            .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.EXCELLENT)
            .map(AuthorDisciplineEntity::getDiscipline)
            .sorted(Comparator.comparing(DisciplineEntity::getName))
            .map(disciplineMapper::map)
            .toList();

        List<DisciplineDto> good = disciplines.stream()
            .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.GOOD)
            .map(AuthorDisciplineEntity::getDiscipline)
            .sorted(Comparator.comparing(DisciplineEntity::getName))
            .map(disciplineMapper::map)
            .toList();

        List<DisciplineDto> maybe = disciplines.stream()
            .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.MAYBE)
            .map(AuthorDisciplineEntity::getDiscipline)
            .sorted(Comparator.comparing(DisciplineEntity::getName))
            .map(disciplineMapper::map)
            .toList();

        return AuthorDisciplinesDto.builder()
            .userId(userId)
            .excellent(excellent)
            .good(good)
            .maybe(maybe)
            .build();
    }

    public List<AuthorFullDto> getAllAuthors() {
        List<AuthorEntity> authorsFetchedAll = authorRepository.getAuthorsFetchedAll();

        Map<Long, UserActivityDto> activityById = userActivityService.getAllUserActivity().stream()
            .collect(Collectors.toMap(UserActivityDto::getId, t -> t));

        Map<Long, List<AuthorOrderDto>> activeOrderIdsById = orderRepository.getActiveOrderCount().stream()
            .collect(Collectors.groupingBy(ActiveOrderCountProjection::getUserId))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                .map(t -> AuthorOrderDto.builder()
                    .orderId(t.getOrderId())
                    .orderTechNumber(t.getOrderTechNumber())
                    .orderStatus(t.getStatus().getStatus().getName())
                    .orderStatusRus(t.getStatus().getStatus().getRusName())
                    .build())
                .toList()));

        return authorsFetchedAll.stream()
            .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
            .map(authorMapper::mapReach)
            .sorted(Comparator.comparing(AuthorFullDto::getUsername))
            .map(t -> t.withActivity(activityById.getOrDefault(t.getId(), new UserActivityDto(t.getId(), t.getUsername(), null))))
            .map(t -> t.withActiveOrders(activeOrderIdsById.getOrDefault(t.getId(), List.of())))
            .toList();
    }
}
