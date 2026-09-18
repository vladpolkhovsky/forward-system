package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.ManagerSubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerSubRepository extends JpaRepository<ManagerSubEntity, Long> {

    Optional<ManagerSubEntity> findBySubManagerId(Long subManagerId);
}
