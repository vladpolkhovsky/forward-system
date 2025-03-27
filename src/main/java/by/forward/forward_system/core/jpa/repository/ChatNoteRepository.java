package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatNoteRepository extends JpaRepository<ChatNoteEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.chat_notes cn where cn.user_id = :userId order by cn.created_at desc")
    List<ChatNoteEntity> findAllByUserId(Long userId);

    @Query(nativeQuery = true, value = "select * from forward_system.chat_notes cn where cn.chat_id = :chatId order by cn.created_at desc")
    List<ChatNoteEntity> findAllByChatId(Long chatId);

    @Modifying
    @Query("delete from ChatNoteEntity where chatId = :chatId")
    void deleteByChatId(Long chatId);

}
