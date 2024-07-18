package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_statuses", schema = "forward_system")
public class OrderStatusEntity {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    public OrderStatus getStatus() {
        return OrderStatus.byName(getName());
    }

}