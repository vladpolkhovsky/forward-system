package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageOptionRepository extends JpaRepository<ChatMessageOptionEntity, Long> {

    @Modifying
    @Query("delete from ChatMessageOptionEntity where message.chat.id = :chatId")
    void deleteByChatId(Long chatId);

    @Modifying
    @Query("update ChatMessageOptionEntity set optionResolved = true where id = :optionId")
    void setOptionResolved(Long optionId);
}
