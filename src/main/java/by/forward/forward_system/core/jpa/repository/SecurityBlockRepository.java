package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.SecurityBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityBlockRepository extends JpaRepository<SecurityBlockEntity, Long> {

    @Query("select s from SecurityBlockEntity s where s.user.id = :userId")
    Optional<SecurityBlockEntity> findByUserId(Long userId);

    Integer countAllByIsPermanentBlockFalse();

    List<SecurityBlockEntity> findAllByIsPermanentBlockFalse();

    @Query("select s from SecurityBlockEntity s where not s.user.deleted and s.isPermanentBlock")
    List<SecurityBlockEntity> findAllByIsPermanentBlockTrue();
}
