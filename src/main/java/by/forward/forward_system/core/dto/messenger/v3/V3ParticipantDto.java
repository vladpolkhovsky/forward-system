package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.enums.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ParticipantDto {
    private Long id;
    private Long orderId;
    private BigDecimal fee;
    private UserDto user;
    private ParticipantType type;
    private String typeRusName;
}
