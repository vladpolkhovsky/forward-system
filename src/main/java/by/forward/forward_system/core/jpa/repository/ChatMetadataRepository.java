package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMetadataRepository extends JpaRepository<ChatMetadataEntity, Long> {
    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.chat_metadata where user_id = :authorId and manager_id = :managerId")
    boolean isChatExists(Long authorId, Long managerId);

    @Query(nativeQuery = true, value = "select distinct cm.id from forward_system.chat_metadata cm where cm.user_id = :id or cm.manager_id = :id")
    List<Long> findChatsByUserId(Long id);
}
