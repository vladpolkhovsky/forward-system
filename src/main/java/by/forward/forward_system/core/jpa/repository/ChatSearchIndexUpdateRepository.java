package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ChatSearchIndexUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSearchIndexUpdateRepository extends JpaRepository<ChatSearchIndexUpdateEntity, Long> {
    @Query(nativeQuery = true, value = "select count(*) > 0 from forward_system.tags where tsvector_tag_name = ''")
    boolean hasEmpty();
}
