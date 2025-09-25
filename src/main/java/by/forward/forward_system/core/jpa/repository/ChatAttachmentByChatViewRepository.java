package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatAttachmentByChatView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAttachmentByChatViewRepository extends JpaRepository<ChatAttachmentByChatView, Long> {

    @EntityGraph(attributePaths = {
        "user",
        "attachment"
    })
    Page<ChatAttachmentByChatView> findAllByChatId(Long chatId, Pageable pageable);
}
