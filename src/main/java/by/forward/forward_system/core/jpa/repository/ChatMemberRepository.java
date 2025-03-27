package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Long> {
    @Modifying
    @Query("delete from ChatMemberEntity where chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
