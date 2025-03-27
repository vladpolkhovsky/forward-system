package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.LastMessageEntity;
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
                where lme.chat.id = :chatId
            union all
            select lme.message.id as id from LastMessageEntity lme
                inner join ChatMessageToUserEntity cmtue on cmtue.message.id = lme.message.id
                where cmtue.user.id = :userId
        ) as r
        """)
    List<Long> newMessageToUserAndForChat(Long userId, Long chatId);

    @Modifying
    @Query(value = "delete from LastMessageEntity where createdAt < :createdAtBefore")
    void deleteAllByCreatedAtBefore(LocalDateTime createdAtBefore);

    @Modifying
    @Query(value = "delete from LastMessageEntity where chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
