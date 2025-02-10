package by.forward.forward_system.core.jpa.repository.projections;

public interface ChatNewMessageProjection {
    Long getChatId();

    Long getNotViewedMessageCount();
}
