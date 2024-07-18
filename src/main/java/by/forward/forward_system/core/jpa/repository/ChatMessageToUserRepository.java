package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageToUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageToUserRepository extends JpaRepository<ChatMessageToUserEntity, Long> {

    @Query(nativeQuery = true, value = "select cmtu.* from forward_system.chat_message_to_user cmtu " +
        "where cmtu.chat_id = :chatId and cmtu.user_id = :userId")
    List<ChatMessageToUserEntity> getAllByUserAndOrder(Long userId, Long chatId);

}
