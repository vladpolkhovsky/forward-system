package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.OrderStatusEntity;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.jpa.repository.projections.SimpleOrderProjection;
import by.forward.forward_system.core.jpa.repository.projections.UserOrderDates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    @Query(nativeQuery = true, value = """
            select
                ords.*
            from
                forward_system.orders ords
            where
                ords.tech_number::int in (
                select
                    distinct (o.tech_number::int)
                from
                    forward_system.orders o
                left join forward_system.order_participants op on
                    o.id = op.order_id
                where
                    op.user_id = :userId and op.type in :participantTypes
                order by
                    o.tech_number::int desc
                limit :limit offset :offset)
            order by
                ords.tech_number::int desc
            """)
    List<OrderEntity> findOrdersWithUserInParticipant(Long userId, int limit, int offset, List<String> participantTypes);

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
            "where o.order_status in :orderStatuses")
    List<OrderEntity> findByStatus(List<String> orderStatuses);

    @Query(nativeQuery = true, value = "SELECT o.id as id, o.tech_number as techNumber, o.name as name, o.order_status as status from forward_system.orders o " +
            "where o.order_status in :orderStatuses " +
            "order by o.tech_number::int8 desc")
    List<SimpleOrderProjection> findByStatusProjection(List<String> orderStatuses);

    @Query(nativeQuery = true, value = """
            select
            	u.firstname as firstname,
            	u.lastname as lastname,
            	u.username as username,
            	a.filename as attachmentFilename,
            	a.id as attachmentFileId,
            	cm.created_at as attachmentTime
            from
            	forward_system.orders o
            inner join forward_system.chats c on
            	c.order_id = o.id
            inner join forward_system.chat_messages cm on
            	c.id = cm.chat_id
            left join forward_system.users u on
            	cm.from_user_id = u.id
            inner join forward_system.chat_message_attachments cma on
            	cma.message_id = cm.id
            inner join forward_system.attachments a on
            	cma.attachment_id = a.id
            where
            	c.type in ('ORDER_CHAT', 'FORWARD_ORDER_CHAT')
            	and c.order_id = :orderId
            order by
            	cm.created_at
            limit 256
            """)
    List<ChatAttachmentProjection> findChatAttachmentsByOrderId(Long orderId);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.orders o where o.order_status != :orderStatus")
    Integer countAllByOrderStatusIsNot(String orderStatus);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.orders o where o.order_status in :orderStatus")
    Integer countAllByOrderStatusIn(List<String> orderStatus);

    @Query(nativeQuery = true, value = "select count(distinct o.id) from forward_system.orders o " +
            "inner join forward_system.order_participants op on o.id = op.order_id " +
            "where op.user_id = :userId and op.type in :types")
    Integer countMyOrders(Long userId, List<String> types);

    @Query(nativeQuery = true, value = "select max(o.tech_number::int)::varchar from forward_system.orders o")
    Optional<String> maxTechNumber();

    @Query(nativeQuery = true, value = """
            select distinct o.tech_number from forward_system.orders o
                inner join forward_system.order_participants op on o.id = op.order_id
                where op.user_id = :userId and op.type in :participantType and o.order_status not in :excludeStatus
            """)
    List<String> getAllTechNumberByParticipant(Long userId, List<String> participantType, List<String> excludeStatus);

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.orders where tech_number::text = :techNumber")
    boolean isTechNumberExists(String techNumber);

    List<OrderEntity> findByTechNumberEquals(String techNumber);

    @Query(nativeQuery = true, value = "select * from forward_system.orders o order by o.tech_number::int desc limit :limit offset :offset")
    List<OrderEntity> findOrderPage(int limit, int offset);

    @Query(nativeQuery = true, value = "select distinct o.id as id, o.tech_number as techNumber, o.intermediate_deadline as intermediateDeadline, o.deadline as deadline, o.additional_dates as additionalDates from forward_system.orders o " +
            "inner join forward_system.order_participants op on op.order_id = o.id " +
            "where op.user_id = :userId and o.order_status not in ('CREATED', 'DISTRIBUTION', 'ADMIN_REVIEW') and op.type in ('HOST', 'MAIN_AUTHOR')")
    List<UserOrderDates> findAllUserOrderEvents(Long userId);

    @Query(value = """
                select COALESCE(sum(o.takingCost), 0) from OrderEntity o
                    inner join OrderParticipantEntity ope on o.id = ope.order.id and ope.participantsType.name = 'CATCHER'
                    where ope.user.id = :catcherId and :from <= o.createdAt and o.createdAt <= :to
            """)
    Long getOrdersSumByTimeAndCatcher(Long catcherId, LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {
        "discipline",
        "orderStatus",
        "createdBy"
    })
    @Query(value = """
            select u from OrderEntity u
                join u.orderParticipants op on op.participantsType.name = 'MAIN_AUTHOR' and op.user.id = :userId
            """)
    List<OrderEntity> getOrderWhereAuthorIs(long userId);

    @EntityGraph(attributePaths = {
        "discipline",
        "orderStatus",
        "createdBy",
        "orderParticipants",
        "orderParticipants.participantsType",
        "orderParticipants.user"
    })
    @Query(value = """
        select u from OrderEntity u
            join u.orderParticipants op on op.user.id = :userId
            where u.orderStatus.name in ('CREATED' , 'DISTRIBUTION', 'ADMIN_REVIEW', 'IN_PROGRESS' , 'REVIEW', 'GUARANTEE', 'FINALIZATION')
                    or (:showClosed is true and u.orderStatus.name = 'CLOSED')
        """)
    List<OrderEntity> getOrderWhereManagerIs(long userId, boolean showClosed);

    @EntityGraph(attributePaths = {
        "discipline",
        "orderStatus",
        "createdBy",
        "orderParticipants",
        "orderParticipants.participantsType",
        "orderParticipants.user"
    })
    @Query(value = "from OrderEntity u where u.id in :ids")
    List<OrderEntity> findAllByIds(List<Long> ids);

    @Query(value = """
            select a.id as userId,
                op.order.id as orderId,
                op.order.techNumber as orderTechNumber,
                op.order.orderStatus as status
            from AuthorEntity a
                inner join OrderParticipantEntity op on op.user = a.user and op.participantsType.name = 'MAIN_AUTHOR'
                where op.order.orderStatus.name in ('FINALIZATION', 'REVIEW', 'IN_PROGRESS')
            """)
    List<ActiveOrderCountProjection> getActiveOrderCount();

    @Modifying
    @Query("UPDATE OrderEntity set orderStatus.name = :statusName where id = :orderId")
    void updateOrderStatus(Long orderId, String statusName);

    @EntityGraph(attributePaths = {
        "orderStatus",
        "discipline",
        "createdBy"
    })
    @Query("from OrderEntity o where o.techNumber like concat('%', :techNumber, '%') order by cast(o.techNumber as int) desc")
    Page<OrderEntity> findOrderIdsByTechNumber(String techNumber, Pageable pageable);

    @EntityGraph(attributePaths = {
        "orderStatus",
        "discipline",
        "createdBy",
        "expertCalendarGroup"
    })
    @Query("""
        from OrderEntity o where o.expertCalendarGroup.id in :groupIds
            and o.orderStatus.name in ('CREATED' , 'DISTRIBUTION', 'ADMIN_REVIEW', 'IN_PROGRESS' , 'REVIEW', 'GUARANTEE', 'FINALIZATION')
        """)
    List<OrderEntity> findByExpertGroupIn(List<Long> groupIds);

    interface ActiveOrderCountProjection {
        Long getUserId();
        Long getOrderId();
        String getOrderTechNumber();
        OrderStatusEntity getStatus();
    }

    @Query(value = "select o.id as id, o.techNumber as techNumber, o.workType as workType, o.discipline.name as discipline, o.subject as subject from OrderEntity o where o.id = :orderId")
    OrderSendRequestProjection findOrderProjectionToOrderSendRequest(Long orderId);

    interface OrderSendRequestProjection {
        Long getId();
        BigDecimal getTechNumber();
        String getWorkType();
        String getDiscipline();
        String getSubject();
    }

    interface OrderIdProjection {
        Long getId();
    }
}
