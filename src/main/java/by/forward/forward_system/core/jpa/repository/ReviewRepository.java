package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where reviewed_by = :currentUserId and not is_reviewed")
    List<ReviewEntity> findAllByUserId(Long currentUserId);

    Integer countAllByIsAcceptedIsFalse();

    @Query(value = "from ReviewEntity where order.id = :orderId and isReviewed order by createdAt desc")
    List<ReviewEntity> findOldReviews(Long orderId);

    @Query("""
        select new by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto(o.techNumber, o.id, r.id, r.createdAt, r.reviewDate, o.name, r.isReviewed, r.isAccepted) from ReviewEntity r
            inner join OrderEntity o on r.order = o
            order by r.createdAt desc
            limit :limit offset :offset
        """)
    List<ReviewProjectionDto> findPage(int offset, int limit);

    @Query(nativeQuery = true, value = "select r.* from forward_system.reviews r inner join forward_system.orders o on r.order_id = o.id where o.tech_number = :techNumber order by created_at desc")
    List<ReviewEntity> findByTechNumber(String techNumber);

    @Query(value = "select count(*) from ReviewEntity where isReviewed = false")
    long countNotReviewed();

    @Query(value = """
        select new by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto(o.techNumber, o.id, r.id, r.createdAt, r.reviewDate, o.name, r.isReviewed, r.isAccepted) from ReviewEntity r
            inner join OrderEntity o on r.order = o
            where r.isReviewed = false
            order by CAST(o.techNumber AS Double) desc, r.reviewDate desc, r.createdAt desc
            limit :limit offset :offset
        """)
    List<ReviewProjectionDto> findAllNotReviewed(int offset, int limit);

    @Query("select id from ReviewEntity where order.id = :orderId")
    List<Long> findAllByOrderId(Long orderId);

    @Query(value = """
        select new by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto(o.techNumber, o.id, r.id, r.createdAt, r.reviewDate, o.name, r.isReviewed, r.isAccepted) from ReviewEntity r
            inner join OrderEntity o on r.order = o
            where r.reviewedBy.id = :userId and r.isReviewed = :isReviewed
            order by CAST(o.techNumber AS Double) desc, r.reviewDate desc, r.createdAt desc
        """)
    List<ReviewProjectionDto> findAllByExpertAndReviewStatusIs(Long userId, boolean isReviewed);

    @EntityGraph(attributePaths = {
        "order",
        "attachment",
        "reviewAttachment",
        "reviewedBy"
    })
    @Query("FROM ReviewEntity where order.id = :orderId order by id desc")
    List<ReviewEntity> fetchReviewByOrderId(Long orderId);
}
