package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.OrderParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderParticipantRepository extends JpaRepository<OrderParticipantEntity, Long> {

}
