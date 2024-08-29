package by.forward.forward_system.core.jpa.repository.projections.table;

import java.util.List;

public interface OrderParticipant {
    List<String> orderTechNumbers();
    int activeOrderCount();
}
