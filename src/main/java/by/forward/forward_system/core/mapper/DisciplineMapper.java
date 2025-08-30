package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.rest.DisciplineDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorDisciplinesDto;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.model.DisciplineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DisciplineMapper {

    DisciplineDto map(DisciplineEntity entity);

    @Mapping(target = "name", source = "discipline.name")
    @Mapping(target = "id", source = "discipline.id")
    DisciplineDto mapAuthorDiscipline(AuthorDisciplineEntity entity);

    @Mapping(target = "maybe", source = "entity", qualifiedByName = "getMaybe")
    @Mapping(target = "good", source = "entity", qualifiedByName = "getGood")
    @Mapping(target = "excellent", source = "entity", qualifiedByName = "getExcellent")
    @Mapping(target = "userId", source = "id")
    AuthorDisciplinesDto map(AuthorEntity entity);

    @Named("getMaybe")
    default List<DisciplineDto> getMaybe(AuthorEntity entity) {
        return Optional.ofNullable(entity)
            .map(e -> getDisciplineByType(e.getAuthorDisciplines(), DisciplineQualityType.MAYBE))
            .orElse(List.of());
    }

    @Named("getGood")
    default List<DisciplineDto> getGood(AuthorEntity entity) {
        return Optional.ofNullable(entity)
            .map(e -> getDisciplineByType(e.getAuthorDisciplines(), DisciplineQualityType.GOOD))
            .orElse(List.of());
    }

    @Named("getExcellent")
    default List<DisciplineDto> getExcellent(AuthorEntity entity) {
        return Optional.ofNullable(entity)
            .map(e -> getDisciplineByType(e.getAuthorDisciplines(), DisciplineQualityType.EXCELLENT))
            .orElse(List.of());
    }

    default List<DisciplineDto> getDisciplineByType(List<AuthorDisciplineEntity> disciplinesDtos, DisciplineQualityType qualityType) {
        if (disciplinesDtos == null) {
            return List.of();
        }
        return disciplinesDtos.stream()
            .filter(t -> t.getDisciplineQuality().getType() == qualityType)
            .map(this::mapAuthorDiscipline)
            .toList();
    }
}
