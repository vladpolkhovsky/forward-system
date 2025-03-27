package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMessageAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageAttachmentRepository extends JpaRepository<ChatMessageAttachmentEntity, Long> {

    @Modifying
    @Query("delete from ChatMessageAttachmentEntity where message.chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
