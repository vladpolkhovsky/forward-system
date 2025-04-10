package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.BotIntegrationDataEntity;
import by.forward.forward_system.core.jpa.model.CustomerTelegramToForwardOrderEntity;
import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.jpa.repository.projections.NeedToSendUpdateForwardOrderProjection;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerTelegramToForwardOrderRepository extends JpaRepository<CustomerTelegramToForwardOrderEntity, Long> {

    @Query("""
        select distinct customer.forwardOrder.id as forwardOrderId, customer.botIntegrationData.telegramChatId as telegramChatId, customer.forwardOrder.chat.id as forwardOrderChatId, customer.forwardOrder.adminChat.id as forwardOrderAdminChatId, customer.lastUpdateAt as lastUpdateAt
            from CustomerTelegramToForwardOrderEntity customer
            where customer.lastUpdateAt <= :lastUpdateAtBefore
        """)
    List<NeedToSendUpdateForwardOrderProjection> findForwardOrdersIdThatNeedToBeUpdated(LocalDateTime lastUpdateAtBefore);

    @Query("""
        select count(*) > 0 from CustomerTelegramToForwardOrderEntity customer where customer.botIntegrationData.telegramChatId = :telegramChatId and customer.botIntegrationData.user is null
        """)
    boolean isChatIdUsedByCustomer(Long telegramChatId);

    @Query("""
        select distinct customer.botIntegrationData from CustomerTelegramToForwardOrderEntity customer
            where customer.forwardOrder.id = :forwardOrderId
        """)
    List<BotIntegrationDataEntity> findForwardOrderCustomers(Long forwardOrderId);

    @Query("""
        select count(customer.id) from CustomerTelegramToForwardOrderEntity customer
            where customer.forwardOrder.id = :forwardOrderId
        """)
    Long countForwardOrderCustomers(Long forwardOrderId);

    List<CustomerTelegramToForwardOrderEntity> findAllByForwardOrder_Id(Long forwardOrderId);
}
