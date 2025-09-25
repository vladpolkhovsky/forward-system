package by.forward.forward_system.core.dto.rest.calendar;

import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3ChatOrderInfoDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarGroupDto {
    private Long id;
    private String name;
    private List<AuthorDto> participants;
    @With
    private List<V3ChatOrderInfoDto> activeOrders;
}
