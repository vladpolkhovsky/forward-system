package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderPaymentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderPaymentStatusRepository extends JpaRepository<OrderPaymentStatusEntity, Long> {

    @Query(value = "from OrderPaymentStatusEntity o join fetch o.user u join fetch u.user join fetch o.order join fetch o.order.orderStatus join fetch o.order.discipline where u.user.id = :userId")
    List<OrderPaymentStatusEntity> findAllByUserId(Long userId);

    @Query(value = "from OrderPaymentStatusEntity o join fetch o.user u join fetch u.user  join fetch o.order join fetch o.order.orderStatus join fetch o.order.discipline where o.id in :ids")
    List<OrderPaymentStatusEntity> findAllByIds(List<Long> ids);

    @Query(value = """
            SELECT DISTINCT ON (user_id, order_id) id
                FROM forward_system.order_payment_status
                WHERE user_id = :userId
                ORDER BY user_id, order_id, created_at DESC, id DESC;
            """, nativeQuery = true)
    List<Long> findLastPaymentStatus(Long userId);
}
