package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import by.forward.forward_system.core.enums.DistributionItemStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionItemLogDto {
    private Long id;
    private Long userId;
    private UserDto user;
    private DistributionItemStatusType statusType;
    private String statusTypeRus;
    private String waitStart;
    private String waitUntil;
    private String createdAt;
}
