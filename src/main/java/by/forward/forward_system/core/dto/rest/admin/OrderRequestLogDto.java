package by.forward.forward_system.core.dto.rest.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestLogDto {
    private Long id;
    private Long managerId;
    private String managerUsername;
    private Long authorId;
    private String authorUsername;
    private Long orderId;
    private String orderTechNumber;
    private String createdAt;
}
