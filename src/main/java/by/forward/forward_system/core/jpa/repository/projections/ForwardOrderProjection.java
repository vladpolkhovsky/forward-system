package by.forward.forward_system.core.jpa.repository.projections;

public interface ForwardOrderProjection {
    Long getId();
    Long getOrderId();
    Long getChatId();
    Long getAdminChatId();
    Long getAuthorUserId();
    Boolean getIsPaymentSend();
    String getAuthorUserUsername();
    String getTechNumber();
    String getAuthorNotes();
    String getAdminNotes();
    String getCode();
}
