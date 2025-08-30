package by.forward.forward_system.core.dto.rest.authors;

import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorFullDto {
    private Long id;
    private String username;
    private AuthorDisciplinesDto disciplines;
    @With
    private UserActivityDto activity;
    @With
    private List<Long> activeOrderIds;
    private Long maxOrdersCount;
}
