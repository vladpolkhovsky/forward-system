package by.forward.forward_system.core.dto.rest.calendar;

import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import lombok.Data;

import java.util.List;

@Data
public class CalendarGroupDto {
    private Long id;
    private String name;
    private List<AuthorDto> participants;
}
