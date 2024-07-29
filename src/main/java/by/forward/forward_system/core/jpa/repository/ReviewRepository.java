package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.reviews where reviewed_by = ?1 and not is_reviewed")
    List<ReviewEntity> findAllByUserId(Long currentUserId);

}
