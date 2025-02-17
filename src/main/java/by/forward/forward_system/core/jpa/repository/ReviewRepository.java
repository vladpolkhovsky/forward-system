package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where reviewed_by = :currentUserId and not is_reviewed")
    List<ReviewEntity> findAllByUserId(Long currentUserId);

    Integer countAllByIsAcceptedIsFalse();

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where order_id = :orderId and is_reviewed order by created_at desc")
    List<ReviewEntity> findOldReviews(Long orderId);

    @Query(nativeQuery = true, value = "select * from forward_system.reviews order by created_at desc limit :limit offset :offset")
    List<ReviewEntity> findPage(int offset, int limit);

    @Query(nativeQuery = true, value = "select r.* from forward_system.reviews r inner join forward_system.orders o on r.order_id = o.id where o.tech_number = :techNumber order by created_at desc")
    List<ReviewEntity> findByTechNumber(String techNumber);

    @Query(nativeQuery = true, value = "select count(*) from forward_system.reviews where is_reviewed = false")
    long countNotReviewed();

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where reviews.is_reviewed = false order by created_at desc limit :limit offset :offset")
    List<ReviewEntity> findAllNotReviewd(int offset, int limit);

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where order_id = :orderId")
    List<ReviewEntity> findAllByOrderId(Long orderId);
}
