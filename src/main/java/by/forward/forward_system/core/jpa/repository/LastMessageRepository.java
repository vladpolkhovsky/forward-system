package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import by.forward.forward_system.core.jpa.model.LastMessageEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LastMessageRepository extends JpaRepository<LastMessageEntity, Long> {

    @Query(value = """
        select distinct r.id from (
            select lme.message.id as id from LastMessageEntity lme
                where lme.chat.id = :chatId and lme.message.createdAt > :after
            union all
            select lme.message.id as id from LastMessageEntity lme
                inner join ChatMessageToUserEntity cmtue on cmtue.message.id = lme.message.id
                where cmtue.user.id = :userId and lme.createdAt > :after
        ) as r
        """)
    @Deprecated
    List<Long> newMessageToUserAndForChat(Long userId, Long chatId, LocalDateTime after);

    @EntityGraph(attributePaths = {
        "chat.order",
        "chat.chatType",
        "chat.chatMetadata",
        "chat.chatMetadata.user",
        "chat.chatMetadata.manager"
    })
    @Query(value = """
        select cm from ChatMessageEntity cm where cm.id in (
            select distinct r.id from (
                select lme.message.id as id from LastMessageEntity lme
                    where lme.chat.id = :chatId and lme.message.createdAt > :after
                union all
                select lme.message.id as id from LastMessageEntity lme
                    inner join ChatMessageToUserEntity cmtue on cmtue.message.id = lme.message.id
                    where cmtue.user.id = :userId and lme.createdAt > :after
            ) as r
        ) order by cm.createdAt
        """)
    List<ChatMessageEntity> newMessageToUserAndForChatFetch(Long userId, Long chatId, LocalDateTime after);

    @Modifying
    @Query(value = "delete from LastMessageEntity where createdAt < :createdAtBefore")
    void deleteAllByCreatedAtBefore(LocalDateTime createdAtBefore);

    @Modifying
    @Query(value = "delete from LastMessageEntity where chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
