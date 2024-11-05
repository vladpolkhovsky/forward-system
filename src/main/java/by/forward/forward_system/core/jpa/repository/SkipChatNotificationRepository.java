package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.SkipChatNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkipChatNotificationRepository extends JpaRepository<SkipChatNotificationEntity, Long> {

    @Query(nativeQuery = true, value = "select scn.chat_id from forward_system.skip_chat_notifications scn where scn.user_id = :userId")
    List<Long> findSkippedChatIdsByUserId(Long userId);

}
