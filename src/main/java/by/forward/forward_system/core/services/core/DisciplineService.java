package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.rest.DisciplineDto;
import by.forward.forward_system.core.jpa.model.DisciplineEntity;
import by.forward.forward_system.core.jpa.repository.DisciplineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;

    public void addDiscipline(String disciplineName) {
        DisciplineEntity disciplineEntity = new DisciplineEntity();

        disciplineEntity.setName(disciplineName);
        disciplineEntity.setId(disciplineRepository.nextDisciplineId());

        disciplineRepository.save(disciplineEntity);
    }

    public void editDiscipline(Long id, String disciplineName) {
        DisciplineEntity discipline = disciplineRepository.findById(id).orElseThrow(() -> new RuntimeException("Discipline not found"));
        discipline.setName(disciplineName);
        disciplineRepository.save(discipline);
    }

    public List<DisciplineDto> allDisciplines() {
        return disciplineRepository.findAll()
            .stream()
            .map(this::toDto)
            .sorted(Comparator.comparing(DisciplineDto::getName))
            .toList();
    }

    private DisciplineDto toDto(DisciplineEntity disciplineEntity) {
        return new DisciplineDto(disciplineEntity.getId(), disciplineEntity.getName());
    }

    public DisciplineDto getById(long id) {
        return disciplineRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new RuntimeException("Discipline not found"));
    }
}
