package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.NotificationDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDataRepository extends JpaRepository<NotificationDataEntity, Long> {

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.notification_data nd where nd.user_id = :userId and nd.unique_number = :notificationId")
    boolean isSubscribed(Long userId, Long notificationId);

    @Query(nativeQuery = true, value = "select * from forward_system.notification_data nd where nd.user_id = :userId")
    List<NotificationDataEntity> loadAllSubscriptions(Long userId);
}
