package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.SavedChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedChatRepository extends JpaRepository<SavedChatEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.chat_saved_to_user cn where cn.user_id = :userId and cn.chat_id = :chatId")
    Optional<SavedChatEntity> findByUserIdAndChatId(Long userId, Long chatId);

    List<SavedChatEntity> findAllByChatId(Long chatId);
}
