package by.forward.forward_system.core.dto.ui;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UpdateOrderRequestDto {
    private Long id;
    private Long orderId;
    private Long fromUserId;
    private BigDecimal orderTechNumber;
    private String newStatus;
    private String newStatusRus;
    private List<Long> catchers;
    private List<Long> hosts;
    private List<Long> authors;
    private List<Long> experts;
    private Boolean isViewed;
    private Boolean isAccepted;
    private LocalDateTime createdAt;
}
