package by.forward.forward_system.core.jpa.repository.projections;

import by.forward.forward_system.core.enums.OrderStatus;

public interface SimpleOrderProjection {
    Long getId();
    String getName();
    String getTechNumber();
    String getStatus();
    default String getOrderStatusRus() {
        return OrderStatus.byName(getStatus()).getRusName();
    }
}
