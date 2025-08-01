package by.forward.forward_system.core.services;

import by.forward.forward_system.core.dto.rest.authors.AuthorDisciplinesDto;
import by.forward.forward_system.core.enums.DisciplineQualityType;
import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.repository.AuthorDisciplineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewAuthorService {

    private final AuthorDisciplineRepository authorDisciplineRepository;

    public AuthorDisciplinesDto getAuthorDisciplines(Long userId) {
        List<AuthorDisciplineEntity> disciplines = authorDisciplineRepository.findUserDisciplinesByUserId(userId);

        List<String> excellent = disciplines.stream()
                .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.EXCELLENT)
                .map(entity -> entity.getDiscipline().getName())
                .sorted().toList();

        List<String> good = disciplines.stream()
                .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.GOOD)
                .map(entity -> entity.getDiscipline().getName())
                .sorted().toList();

        List<String> maybe = disciplines.stream()
                .filter(entity -> entity.getDisciplineQuality().getType() == DisciplineQualityType.MAYBE)
                .map(entity -> entity.getDiscipline().getName())
                .sorted().toList();

        return AuthorDisciplinesDto.builder()
                .userId(userId)
                .excellent(excellent)
                .good(good)
                .maybe(maybe)
                .build();
    }

}
