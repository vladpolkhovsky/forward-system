package by.forward.forward_system.core.jpa.repository.projections;

public interface OrderChatDataProjection {
    Long getChatId();
    Long getOrderId();
    Integer getNewMessageCount();
}
