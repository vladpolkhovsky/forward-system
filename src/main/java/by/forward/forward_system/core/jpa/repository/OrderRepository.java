package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.jpa.repository.projections.SimpleOrderProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT distinct o.* from forward_system.orders o " +
                                       "left join forward_system.order_participants op on o.id = op.order_id " +
                                       "where op.user_id = :userId")
    List<OrderEntity> findOrdersWithUserInParticipant(Long userId);

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
                                       "where o.order_status in :orderStatuses")
    List<OrderEntity> findByStatus(List<String> orderStatuses);

    @Query(nativeQuery = true, value = "SELECT o.id as id, o.tech_number as techNumber, o.name as name, o.order_status as status from forward_system.orders o " +
                                       "where o.order_status in :orderStatuses " +
                                       "order by o.tech_number::int8 desc")
    List<SimpleOrderProjection> findByStatusProjection(List<String> orderStatuses);

    @Query(nativeQuery = true, value = "select u.firstname as firstname, u.lastname as lastname, u.username as username, a.filename as attachmentFilename, a.id as attachmentFileId, cm.created_at as attachmentTime from forward_system.orders o " +
                                       "inner join forward_system.chats c on c.order_id = o.id " +
                                       "inner join forward_system.chat_messages cm on c.id = cm.chat_id " +
                                       "inner join forward_system.users u on cm.from_user_id = u.id " +
                                       "inner join forward_system.chat_message_attachments cma on cma.message_id = cm.id " +
                                       "inner join forward_system.attachments a on cma.attachment_id = a.id " +
                                       "where c.type = 'ORDER_CHAT' and c.order_id = :orderId " +
                                       "order by cm.created_at limit 256")
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

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.orders where tech_number = :techNumber")
    boolean isTechNumberExists(String techNumber);

    List<OrderEntity> findByTechNumberEquals(String techNumber);

    @Query(nativeQuery = true, value = "select * from forward_system.orders o order by o.tech_number::int desc limit :limit offset :offset")
    List<OrderEntity> findOrderPage(int limit, int offset);
}
