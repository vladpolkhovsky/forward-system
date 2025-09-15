package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.QueueDistributionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueDistributionRepository extends JpaRepository<QueueDistributionEntity, Long> {

    @EntityGraph(attributePaths = {
        "order",
        "order.orderStatus",
        "order.discipline",
        "createdBy",
        "items",
        "items.user",
    })
    @Query(value = "FROM QueueDistributionEntity where order.id = :orderId order by createdAt")
    List<QueueDistributionEntity> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {
        "order",
        "order.orderStatus",
        "order.discipline",
        "order.createdBy",
        "order.orderParticipants",
        "order.orderParticipants.user",
        "order.orderParticipants.participantsType",
        "createdBy"
    })
    @Query(value = "FROM QueueDistributionEntity where status = 'IN_PROGRESS'")
    List<QueueDistributionEntity> findInProgress();

    @Query(value = """
        select count(*) > 0 from QueueDistributionEntity q
                inner join QueueDistributionItemEntity qi on qi.queueDistribution = q
                where q.status = 'IN_PROGRESS' and q.order.id = :orderId and qi.user.id = :authorId
        """)
    boolean isUserInInProgressDistribution(Long orderId, Long authorId);

    @Modifying
    void deleteAllByOrder_Id(Long orderId);
}
