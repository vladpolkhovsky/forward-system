package by.forward.forward_system.core.dto.messenger;

import by.forward.forward_system.core.enums.OrderSourceType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private String name;
    private BigDecimal techNumber;
    private String orderStatus;
    private String orderStatusRus;
    private String workType;
    private String discipline;
    private String subject;
    private String amount;
    private String verificationSystem;
    private Boolean verifyPlanOnAccept;
    private String additionalDates;
    private Integer originality;
    private LocalDateTime intermediateDeadline;
    private LocalDateTime deadline;
    private String other;
    private String violationsInformation;
    private OrderSourceType orderSource;
    private Integer authorCost;
    private Integer takingCost;
    private LocalDateTime createdAt;
    private List<OrderAttachmentDto> attachments;
    private List<OrderParticipantDto> participants;
}
