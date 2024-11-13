package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderParticipantRepository extends JpaRepository<OrderParticipantEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.order_participants op where op.order_id = :orderId")
    List<OrderParticipantEntity> getOrderParticipantsByOrderId(Long orderId);

}
