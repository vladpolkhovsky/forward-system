package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
        "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
        "where chat_members.user_id = :userId and chat.chat_name = :name order by chat.last_message_date desc")
    Optional<ChatEntity> findChatByUserAndChatName(Long userId, String name);

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
        "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
        "where chat_members.user_id = :userId order by chat.last_message_date desc")
    List<ChatEntity> findChatByUser(Long userId);

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
        "where chat.chat_name like 'Чат с Администрацией%'")
    List<ChatEntity> findAdminTalkChats();

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.chats chat " +
        "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
        "where chat.id = :chatId and chat_members.user_id = :userId")
    boolean isChatMember(Long chatId, Long userId);
}
