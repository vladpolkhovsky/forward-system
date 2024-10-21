package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.BotNotificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotNotificationCodeRepository extends JpaRepository<BotNotificationCodeEntity, Long> {
    boolean existsByCode(String code);
    Optional<BotNotificationCodeEntity> findByCode(String code);
}
