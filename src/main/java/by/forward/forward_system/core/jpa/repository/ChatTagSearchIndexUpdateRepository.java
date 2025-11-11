package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatTagSearchIndexUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatTagSearchIndexUpdateRepository extends JpaRepository<ChatTagSearchIndexUpdateEntity, Long> {
    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.chats where tsvector_chat_name = ''")
    boolean hasEmpty();
}
