package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.repository.projections.ChatNewMessageProjection;
import by.forward.forward_system.core.jpa.repository.projections.ChatProjection;
import by.forward.forward_system.core.jpa.repository.projections.NewMessageCountProjection;
import by.forward.forward_system.core.jpa.repository.projections.OrderChatDataProjection;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query(nativeQuery = true, value = "select c.* from forward_system.chats c " +
                                       "inner join forward_system.chat_metadata cm on cm.id = c.id " +
                                       "where cm.user_id = :userId and cm.manager_id = :managerId")
    Optional<ChatEntity> findChatEntityByUserAndManagerId(Long userId, Long managerId);

    @Query(nativeQuery = true, value = "select chat.id, chat.chat_name, chat.type, chat.last_message_date from forward_system.chats chat")
    List<ChatProjection> findAllChatsProjection();

    @Query(nativeQuery = true, value = "select chat.id, chat.chat_name, chat.type, chat.last_message_date from forward_system.chats chat where chat.id in :chatIds")
    List<ChatProjection> findChatsProjection(List<Long> chatIds);

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
                                       "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
                                       "where chat_members.user_id = :userId order by chat.last_message_date desc")
    List<ChatEntity> findChatByUser(Long userId);

    @Query(nativeQuery = true, value = "select chat.id, chat.chat_name, chat.type, chat.last_message_date from forward_system.chats chat " +
                                       "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
                                       "where chat_members.user_id = :userId order by chat.last_message_date desc")
    List<ChatProjection> findChatByUserProjection(Long userId);

    @Query(nativeQuery = true, value = "select cmtu.chat_id, count(*) as not_viewed_message_count from forward_system.chat_message_to_user cmtu " +
                                       " where cmtu.user_id = :userId and cmtu.is_viewed = false " +
                                       " group by cmtu.chat_id")
    List<ChatNewMessageProjection> findNewMessageProjection(Long userId);

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
                                       "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
                                       "where chat_members.user_id = :userId and chat.id = :chatId order by chat.last_message_date desc")
    List<ChatEntity> findChatByUserAndChatId(Long userId, Long chatId);

    @Query(nativeQuery = true, value = "select chat.* from forward_system.chats chat " +
                                       "where chat.chat_name like 'Чат с Администрацией%'")
    List<ChatEntity> findAdminTalkChats();

    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.chats chat " +
                                       "left join forward_system.chat_members chat_members on chat.id = chat_members.chat_id " +
                                       "where chat.id = :chatId and chat_members.user_id = :userId")
    boolean isChatMember(Long chatId, Long userId);

    @Query(nativeQuery = true, value = "select c.chat_name from forward_system.chats c where c.id = :chatId")
    Optional<String> findChatNameById(Long chatId);

    @Query(nativeQuery = true, value = """
        select c.* from forward_system.chats c
        	inner join forward_system.chat_metadata cm on cm.id = c.id
        	where cm.user_id = :userId and c."type" = 'ADMIN_TALK_CHAT' and c.id != 0
        """)
    Optional<ChatEntity> findUserAdminChat(Long userId);

    List<ChatEntity> order(OrderEntity order);

    @Query(nativeQuery = true, value = """
            select c.id as chatId, c.order_id as orderId, count(distinct cmtu.id) as newMessageCount from forward_system.chats c
            	inner join forward_system.chat_members cm on cm.chat_id = c.id
            	left join forward_system.chat_message_to_user cmtu on cmtu.chat_id = c.id and cmtu.user_id = :userId and not cmtu.is_viewed
            	where cm.user_id = :userId and c.order_id in :orderIds
            	group by c.order_id, c.id
        """)
    List<OrderChatDataProjection> findOrderChatNewMessagesForUser(List<Long> orderIds, Long userId);

    @EntityGraph(attributePaths = {
        "order",
        "tags",
        "chatType",
        "chatMetadata",
        "chatMetadata.user",
        "chatMetadata.manager"
    })
    @Query("FROM ChatEntity WHERE id in :ids order by lastMessageDate desc")
    List<ChatEntity> fetchDataToChatResponse(List<Long> ids);

    @Query("""
            select chat.id as chatId, count(cmtu.id) as newMessageCount from ChatEntity chat
                left join ChatMessageToUserEntity cmtu on cmtu.chat = chat and cmtu.user.id = :userId
                where chat.id in :chatIds and cmtu.isViewed is false
                group by chat.id
        """)
    List<NewMessageCountProjection> findNewMessageCount(List<Long> chatIds, Long userId);

    interface NewMessageCountProjection {
        Long getChatId();
        Integer getNewMessageCount();
    }

    @Query(value = """
        select chat.chatType.name as type, count(distinct chat.id) as count from ChatEntity chat
            inner join ChatMessageToUserEntity messageToUser on messageToUser.chat = chat
            where messageToUser.isViewed is false and messageToUser.user.id = :userId
            group by chat.chatType
        """)
    List<ChatTypeToChatsWithNewMessageCount> findChatTypeToChatsWithNewMessageCount(Long userId);

    interface ChatTypeToChatsWithNewMessageCount {
        String getType();
        Integer getCount();
    }
}
