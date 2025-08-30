package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.enums.DistributionStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionLogDto {
    private Long id;
    private Long orderId;
    private List<DistributionItemLogDto> items;
    private DistributionStatusType statusType;
    private String statusTypeRus;
    private UserDto createdBy;
    private String createdAt;
}
