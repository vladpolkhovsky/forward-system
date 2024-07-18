package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

}
