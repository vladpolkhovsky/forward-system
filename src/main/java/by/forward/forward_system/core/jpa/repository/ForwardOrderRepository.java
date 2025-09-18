package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.jpa.repository.projections.ForwardOrderProjection;
import by.forward.forward_system.core.jpa.repository.projections.LastMessageDateByChatIdProjection;
import by.forward.forward_system.core.jpa.repository.projections.NewMessageCountProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ForwardOrderRepository extends JpaRepository<ForwardOrderEntity, Long> {

    @EntityGraph(attributePaths = {
        "chat.chatMetadata"
    })
    Optional<ForwardOrderEntity> findByOrder_Id(Long orderId);

    Optional<ForwardOrderEntity> findByCode(String code);

    @Query("""
        select foe.id,
               foe.chat.id as chatId,
               foe.adminChat.id as adminChatId,
               foe.isPaymentSend as paid,
               foe.chat.chatMetadata.authorCanSubmitFiles as authorCanSubmitFiles
       from ForwardOrderEntity foe
            where foe.chat.id in :chatIds or foe.adminChat.id in :chatIds
        """)
    List<ForwardOrderStatusProjection> findByChatIds(List<Long> chatIds);

    interface ForwardOrderStatusProjection {
        Long getId();
        Long getChatId();
        Long getAdminChatId();
        Boolean getPaid();
        Boolean getAuthorCanSubmitFiles();
    }

    @Query(value = """
        select distinct c.id as chatId, c.lastMessageDate as lastMessageDate from ChatEntity c
            where c.id in :chatIds
        """)
    List<LastMessageDateByChatIdProjection> findLastMessageDateByChatId(List<Long> chatIds);

    @Query(value = """
        select foe.chat.id as id, count(*) as count from ForwardOrderEntity foe
            inner join ChatMessageToUserEntity cmtue on cmtue.chat.id = foe.chat.id
            where cmtue.user.id = :currentUserId and cmtue.isViewed = false
            group by foe.chat.id
        union all
        select foe.adminChat.id as id, count(*) as count from ForwardOrderEntity foe
            inner join ChatMessageToUserEntity cmtue on cmtue.chat.id = foe.adminChat.id
            where cmtue.user.id = :currentUserId and cmtue.isViewed = false
            group by foe.adminChat.id
        """)
    List<NewMessageCountProjection> calcNewMessageCount(Long currentUserId);

    @Query(value = """
        select foe.id as id,
               foe.order.id as orderId,
               foe.chat.id as chatId,
               foe.author.id as authorUserId,
               foe.author.username as authorUserUsername,
               foe.adminChat.id as adminChatId,
               foe.order.techNumber as techNumber,
               foe.adminNotes as adminNotes,
               foe.authorNotes as authorNotes,
               foe.isPaymentSend as isPaymentSend,
               foe.code as code,
               foe.createdAt as createdAt
        from ForwardOrderEntity foe
        """)
    List<ForwardOrderProjection> findAllProjections();

    @Query(value = """
        select foe.chat.chatMetadata.authorCanSubmitFiles from ForwardOrderEntity foe
            where foe.id = :forwardOrderId
        """)
    boolean isEnabledFileSubmission(Long forwardOrderId);

    @Query("""
        select foe.id as id,
               foe.order.id as orderId,
               foe.chat.id as chatId,
               foe.author.id as authorUserId,
               foe.author.username as authorUserUsername,
               foe.adminChat.id as adminChatId,
               foe.order.techNumber as techNumber,
               foe.adminNotes as adminNotes,
               foe.authorNotes as authorNotes,
               foe.isPaymentSend as isPaymentSend,
               foe.code as code,
               foe.createdAt as createdAt
        from ForwardOrderEntity foe
        inner join CustomerTelegramToForwardOrderEntity customer on customer.forwardOrder = foe
        inner join BotIntegrationDataEntity integrationData on customer.botIntegrationData = integrationData
        where integrationData.id = :botIntegrationDataId
        """)
    List<ForwardOrderProjection> getCustomerOrdersByBotIntegrationDataId(long botIntegrationDataId);

    @Query("""
        select foe.id from ForwardOrderEntity foe where foe.chat.id = :chatId or foe.adminChat.id = :chatId
        """)
    Optional<Long> findForwardOrderIdByChatId(Long chatId);

    @Query("""
        select foe from ForwardOrderEntity foe where foe.chat.id = :chatId or foe.adminChat.id = :chatId
        """)
    Optional<ForwardOrderEntity> findForwardOrderByChatId(Long chatId);

    @Query("""
        select foe.chat from ForwardOrderEntity foe where foe.chat.id = :chatId
        union all
        select foe.adminChat from ForwardOrderEntity foe where foe.adminChat.id = :chatId
        """)
    Optional<ChatEntity> findForwardOrderChatByChatId(Long chatId);

    @Query("""
        select distinct foe.chat.id from ForwardOrderEntity foe
             join foe.chat.participants p
             where foe.chat.id in :chatIds and p.id = :userId
        """)
    Set<Long> findChatParticipants(Long userId, List<Long> chatIds);

    @Query("""
        select foe.chat.chatMetadata.authorCanSubmitFiles from ForwardOrderEntity foe
                where foe.id = :forwardOrderId
        """)
    Optional<Boolean> getForwardOrderFileSubmissionStatus(Long forwardOrderId);

    @Query("""
        select foe.isPaymentSend from ForwardOrderEntity foe
                where foe.id = :forwardOrderId
        """)
    Optional<Boolean> getForwardOrderPaidStatus(Long forwardOrderId);
}
