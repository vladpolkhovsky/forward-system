package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
        "left join forward_system.order_participants op on o.id = op.order_id " +
        "where op.user_id = :userId")
    List<OrderEntity> findOrdersWithUserInParticipant(Long userId);

    @Query(nativeQuery = true, value = "SELECT o.* from forward_system.orders o " +
        "where o.order_status in :orderStatuses")
    List<OrderEntity> findByStatus(List<String> orderStatuses);
}
