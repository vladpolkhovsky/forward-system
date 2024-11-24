package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.enums.PaymentStatus;

import java.time.LocalDateTime;

public interface PaymentDto {
    Long getId();
    Long getNumber();

    String getStatusName();
    String getUsername();

    LocalDateTime getCreated();
    LocalDateTime getUpdated();

    default PaymentStatus getStatus() {
        return PaymentStatus.byName(getStatusName());
    }
}
