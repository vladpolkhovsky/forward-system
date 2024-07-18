package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.UpdateOrderRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateOrderRequestRepository extends JpaRepository<UpdateOrderRequestEntity, Long> {

    @Query(nativeQuery = true, value = "select r.* from forward_system.update_order_request r " +
        "where r.is_viewed = false order by created_at")
    List<UpdateOrderRequestEntity> getNotReviewedOrderRequests();

}
