package by.forward.forward_system.core.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderUiDto {
    private Long id;
    private BigDecimal techNumber;
    private String name;
    private String workType;
    private String discipline;
    private String subject;
    private Integer originality;
    private String orderStatus;
    private String orderStatusRus;
    private String verificationSystem;
    private LocalDateTime intermediateDeadline;
    private LocalDateTime deadline;
    private String other;
    private Integer takingCost;
    private Integer authorCost;
    private LocalDateTime createdAt;
}
