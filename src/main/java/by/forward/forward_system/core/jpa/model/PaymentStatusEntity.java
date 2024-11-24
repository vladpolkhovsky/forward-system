package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_status", schema = "forward_system")
public class PaymentStatusEntity {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public PaymentStatus getStatus() {
        return PaymentStatus.byName(name);
    }

}