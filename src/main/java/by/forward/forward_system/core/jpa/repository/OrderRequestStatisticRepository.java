package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderRequestStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRequestStatisticRepository extends JpaRepository<OrderRequestStatisticEntity, Long>, JpaSpecificationExecutor<OrderRequestStatisticEntity> {

    @Query("""
            select ors.id as id, 
                    ors.manager as managerId,
                    manager.username as managerUsername,
                    ors.author as authorId,
                    author.username as authorUsername,
                    ors.orderId as orderId,
                    order.techNumber as orderTechNumber,
                    ors.createdAt as createdAt
                from OrderRequestStatisticEntity ors 
                    join UserEntity manager on ors.manager = manager.id 
                    join UserEntity author on ors.author = author.id
                    left join OrderEntity order on ors.orderId = order.id
                where ors.id in :ids
        """)
    List<InfoProjection> findByIdsProjection(List<Long> ids);

    interface IdProjection {
        Long getId();
    }

    interface InfoProjection {
        Long getId();
        Long getManagerId();
        String getManagerUsername();
        Long getAuthorId();
        String getAuthorUsername();
        Long getOrderId();
        String getOrderTechNumber();
        LocalDateTime getCreatedAt();
    }
}
