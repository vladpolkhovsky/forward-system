package by.forward.forward_system.core.jpa.repository.projections;

import java.time.LocalDateTime;

public interface LastMessageDateByChatIdProjection {
    Long getChatId();
    LocalDateTime getLastMessageDate();
}
