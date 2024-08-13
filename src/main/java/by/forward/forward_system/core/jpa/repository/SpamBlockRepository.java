package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.SpamBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpamBlockRepository extends JpaRepository<SpamBlockEntity, Long> {

    @Query("select s from SpamBlockEntity s where s.user.id = :userId")
    Optional<SpamBlockEntity> findByUserId(Long userId);

    Integer countAllByIsPermanentBlockFalse();

    List<SpamBlockEntity> findAllByIsPermanentBlockFalse();
}
