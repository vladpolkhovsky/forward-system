package by.forward.forward_system.core.jpa.repository.projections;

public interface ForwardOrderProjection {
    Long getId();
    Long getOrderId();
    Long getChatId();
    Long getAdminChatId();
    Long getAuthorUserId();
    String getAuthorUserUsername();
    String getTechNumber();
}
