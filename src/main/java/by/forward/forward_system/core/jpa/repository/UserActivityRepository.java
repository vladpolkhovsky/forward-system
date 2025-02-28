package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.UserActivityEntity;
import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivityEntity, Long> {

    @Modifying
    @Query("update UserActivityEntity set lastOnlineDate = :now where userId = :userId")
    void updateLastOnlineDate(Long userId, LocalDateTime now);

    default void updateLastOnlineDate(Long userId) {
        updateLastOnlineDate(userId, LocalDateTime.now());
    }

    @Query("select new by.forward.forward_system.core.jpa.repository.projections.UserActivityDto(u.id, u.username, uae.lastOnlineDate) from UserEntity u " +
           "left join UserActivityEntity uae on u.id = uae.userId")
    List<UserActivityDto> loadAllUserActivity();
}
