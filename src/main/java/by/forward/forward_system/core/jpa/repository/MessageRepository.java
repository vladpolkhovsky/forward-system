package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query(nativeQuery = true, value = """
        select count(distinct c.chat_id) from forward_system.chat_message_to_user c
                inner join forward_system.chats chat on c.chat_id = chat.id
                where c.user_id = :currentUserId and c.is_viewed = false 
                and chat.type not in ('FORWARD_ORDER_CHAT', 'FORWARD_ORDER_ADMIN_CHAT')
        """)
    Integer getNewMessageByUserId(Long currentUserId);

    @Query(nativeQuery = true, value = """
        select count(distinct c.chat_id) from forward_system.chat_message_to_user c
                inner join forward_system.chats chat on c.chat_id = chat.id
                where c.user_id = :currentUserId and c.is_viewed = false 
                and chat.type in ('FORWARD_ORDER_CHAT', 'FORWARD_ORDER_ADMIN_CHAT')
        """)
    Integer getNewForwardMessageByUserId(Long currentUserId);

    @Query(value = "select m from ChatMessageEntity m where m.fromUser.id = :userId order by m.createdAt desc limit :limit")
    List<ChatMessageEntity> findAllByUser(Long userId, int limit);

    @Query(nativeQuery = true, value = "select u.username from forward_system.chat_members member" +
                                       "    left join forward_system.chat_messages message on message.chat_id = member.chat_id" +
                                       "    inner join forward_system.users u on member.user_id = u.id" +
                                       "    left join forward_system.chat_message_to_user cmtu on cmtu.user_id = u.id and cmtu.message_id = message.id" +
                                       "    where message.id = :messageId and (cmtu.is_viewed or cmtu.is_viewed is null)" +
                                       "    order by u.username ")
    List<String> findWhoReadMessage(Long messageId);

    @Modifying
    @Query("delete from ChatMessageEntity where chat.id = :chatId")
    void deleteByChatId(Long chatId);

    @EntityGraph(attributePaths = {
        "chat",
        "chat.order",
        "chat.order.orderParticipants",
        "chat.order.orderParticipants.user",
        "chat.participants",
        "chat.chatMetadata",
        "chatMessageType",
        "chatMessageAttachmentsSet",
        "chatMessageAttachmentsSet.attachment",
        "chatMessageOptionsSet",
        "chatMessageOptionsSet.orderParticipant",
        "chatMessageToUsersSet",
        "chatMessageToUsersSet.user",
        "fromUser"
    })
    @Query("from ChatMessageEntity where chat.id = :chatId and createdAt < :afterTime order by createdAt desc ")
    Page<ChatMessageEntity> search(Long chatId, LocalDateTime afterTime, Pageable pageable);


    @EntityGraph(attributePaths = {
        "chat",
        "chat.order",
        "chat.participants",
        "chat.chatMetadata",
        "chatMessageType",
        "fromUser"
    })
    @Query("from ChatMessageEntity where id = :id")
    ChatMessageEntity findByIdAndFetch(Long id);
}
