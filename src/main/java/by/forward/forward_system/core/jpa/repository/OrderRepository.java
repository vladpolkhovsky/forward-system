package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
        "left join forward_system.order_participants op on o.id = op.order_id " +
        "where op.user_id = :userId")
    List<OrderEntity> findOrdersWithUserInParticipant(Long userId);

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
        "where o.order_status in :orderStatuses")
    List<OrderEntity> findByStatus(List<String> orderStatuses);

    @Query(nativeQuery = true, value = "select u.firstname as firstname, u.lastname as lastname, a.filename as attachmentFilename, a.id as attachmentFileId, cm.created_at as attachmentTime from forward_system.orders o " +
        "inner join forward_system.chats c on c.order_id = o.id " +
        "inner join forward_system.chat_messages cm on c.id = cm.chat_id " +
        "inner join forward_system.users u on cm.from_user_id = u.id " +
        "inner join forward_system.chat_message_attachments cma on cma.message_id = cm.id " +
        "inner join forward_system.attachments a on cma.attachment_id = a.id " +
        "where c.type = 'ORDER_CHAT' and c.order_id = :orderId " +
        "order by cm.created_at")
    List<ChatAttachmentProjection> findChatAttachmentsByOrderId(Long orderId);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.orders o where o.order_status != :orderStatus")
    Integer countAllByOrderStatusIsNot(String orderStatus);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.orders o where o.order_status in :orderStatus")
    Integer countAllByOrderStatusIn(List<String> orderStatus);

    @Query(nativeQuery = true, value = "select count(distinct o.id) from forward_system.orders o " +
        "inner join forward_system.order_participants op on o.id = op.order_id " +
        "where op.user_id = :userId")
    Integer countMyOrders(Long userId);

    @Query(value = "select MAX(o.techNumber) from OrderEntity o")
    Optional<String> maxTechNumber();
}
