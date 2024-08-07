package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query(nativeQuery = true, value = "select count(*) from forward_system.chat_message_to_user c" +
        " where c.user_id = :currentUserId and c.is_viewed = false;")
    Integer getNewMessageByUserId(Long currentUserId);
}
