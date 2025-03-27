package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageToUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageToUserRepository extends JpaRepository<ChatMessageToUserEntity, Long> {

    @Query(nativeQuery = true, value = "select cmtu.* from forward_system.chat_message_to_user cmtu " +
                                       "where cmtu.chat_id = :chatId and cmtu.user_id = :userId")
    List<ChatMessageToUserEntity> getAllByUserAndChat(Long userId, Long chatId);

    @Modifying
    @Query(nativeQuery = true, value = "update forward_system.chat_message_to_user set is_viewed = true where user_id = :userId")
    void setAllViewed(Long userId);

    @Modifying
    @Query(nativeQuery = true, value = "update forward_system.chat_message_to_user cmtu set is_viewed = true where user_id = :userId and exists(select * from forward_system.chats c where cmtu.chat_id = c.id and c.type = :chatType)")
    void setAllViewed(Long userId, String chatType);

    @Query(nativeQuery = true, value = "select distinct cmtu.user_id from forward_system.chat_message_to_user cmtu where cmtu.is_viewed = false")
    List<Long> getAllNotViewed();

    @Query(nativeQuery = true, value = "select cmtu.id from forward_system.chat_message_to_user cmtu where cmtu.is_viewed and cmtu.created_at < :olderThen order by cmtu.created_at limit 3000")
    List<Long> getAllViewedOlderThen(LocalDateTime olderThen);

    @Modifying
    @Query("delete from ChatMessageToUserEntity where chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
