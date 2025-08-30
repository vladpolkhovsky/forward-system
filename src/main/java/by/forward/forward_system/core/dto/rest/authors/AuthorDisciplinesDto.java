package by.forward.forward_system.core.dto.rest.authors;

import by.forward.forward_system.core.dto.rest.DisciplineDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthorDisciplinesDto {
    private Long userId;
    private List<DisciplineDto> excellent;
    private List<DisciplineDto> good;
    private List<DisciplineDto> maybe;
}
