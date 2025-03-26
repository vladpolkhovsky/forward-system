package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ForwardOrderReviewRequestEntity;
import by.forward.forward_system.core.jpa.repository.projections.ReviewRequestProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForwardOrderReviewRequestRepository extends JpaRepository<ForwardOrderReviewRequestEntity, Long> {
    @Query(value = """
        select rr.id as id,
                rr.forwardOrder.id as forwardOrderId,
                rr.note as note,
                rr.requestByUser.username as authorUsername,
                rr.file.id as fileId,
                rr.createdAt as createdAt,
                rr.done as done,
                r.id as reviewId,
                r.reviewMark as reviewMark,
                r.isReviewed as reviewIsReviewed,
                r.reviewDate reviewReviewedAt
            from ForwardOrderReviewRequestEntity rr
            left join ReviewEntity r on r.id = rr.review.id
            where rr.forwardOrder.id = :forwardOrderId
            order by rr.createdAt desc
        """)
    List<ReviewRequestProjection> findReviewProjectionsByForwardOrder_Id(Long forwardOrderId);

    Optional<ForwardOrderReviewRequestEntity> findFirstByReview_Id(Long reviewId);
}
