package by.forward.forward_system.core.dto.rest.authors;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthorDisciplinesDto {
    private Long userId;
    private List<String> excellent;
    private List<String> good;
    private List<String> maybe;
}
