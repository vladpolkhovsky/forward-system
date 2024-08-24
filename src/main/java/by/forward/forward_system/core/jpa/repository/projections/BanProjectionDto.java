package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BanProjectionDto {
    private Long id;
    private UserDto user;
    private String reason;
    private MessageDto message;
    private List<MessageDto> lastMessages;
}
