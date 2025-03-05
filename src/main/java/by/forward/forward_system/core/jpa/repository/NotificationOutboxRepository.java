package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEntity, Long> {
    @Query(nativeQuery = true, value = "select * from forward_system.notification_outbox no where no.chat_id = :chatId")
    List<NotificationOutboxEntity> findAllByChatId(Long chatId);

    @Query(nativeQuery = true, value = "select * from forward_system.notification_outbox no where no.created_at < :time;")
    List<NotificationOutboxEntity> getAllMessagesOlderThen(LocalDateTime time);

    @Query(nativeQuery = true, value = "select * from forward_system.notification_outbox no where no.chat_id = :chatId;")
    List<NotificationOutboxEntity> getAllMessagesByChatId(Long chatId);

    @Query(nativeQuery = true, value = "delete from forward_system.notification_outbox no where no.user_id = :userId")
    @Modifying
    void deleteAllByUserId(Long userId);
}
