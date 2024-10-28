package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.AiViolationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiViolationRepository extends JpaRepository<AiViolationEntity, Long> {

    @Query(nativeQuery = true, value = "select count(*) from forward_system.ai_violations av where av.created_at > :from and av.user_id = :userId and not av.is_old_violation")
    int violationsCount(Long userId, LocalDateTime from);

    @Query(nativeQuery = true, value = "select * from forward_system.ai_violations av order by av.created_at desc limit :limit offset :offset")
    List<AiViolationEntity> getPage(int limit, int offset);

    @Query(nativeQuery = true, value = "select * from forward_system.ai_violations av where av.user_id = :userId and not av.is_old_violation")
    List<AiViolationEntity> searchByUserId(Long userId);
}
