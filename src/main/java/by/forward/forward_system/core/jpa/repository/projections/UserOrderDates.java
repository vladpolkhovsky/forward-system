package by.forward.forward_system.core.jpa.repository.projections;

import java.time.LocalDateTime;

public interface UserOrderDates {
    Long getId();
    String getTechNumber();
    LocalDateTime getIntermediateDeadline();
    LocalDateTime getDeadline();
    String getAdditionalDates();
}
