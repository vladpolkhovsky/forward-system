package by.forward.forward_system.core.jpa.repository.projections;

public interface NeedToSendUpdateForwardOrderProjection {
    Long getForwardOrderId();
    Long getTelegramChatId();
    Long getForwardOrderChatId();
    Long getForwardOrderAdminChatId();
    Long getLastUpdateAt();
}
