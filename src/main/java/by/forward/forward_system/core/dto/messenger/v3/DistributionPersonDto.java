package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.dto.rest.authors.AuthorFullDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionPersonDto {
    private Long userId;
    private AuthorFullDto author;
    private Integer customFee;
    private Long order;
}
