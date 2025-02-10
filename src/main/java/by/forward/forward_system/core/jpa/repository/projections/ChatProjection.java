package by.forward.forward_system.core.jpa.repository.projections;

import java.time.LocalDateTime;

public interface ChatProjection {
    Long getId();

    String getChatName();

    String getType();

    LocalDateTime getLastMessageDate();
}
