package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.CalendarGroupParticipantStatusEntity;
import by.forward.forward_system.core.jpa.model.CalendarGroupParticipantStatusEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
public interface CalendarGroupParticipantStatusRepository extends JpaRepository<CalendarGroupParticipantStatusEntity, CalendarGroupParticipantStatusEntityId> {

    @Query("from CalendarGroupParticipantStatusEntity cgp where :begin <= cgp.id.date and cgp.id.date <= :end and cgp.id.groupId = :groupId")
    Set<CalendarGroupParticipantStatusEntity> findGroupWorkStatusBetween(LocalDate begin, LocalDate end, Long groupId);

    @Modifying
    @Query("delete from CalendarGroupParticipantStatusEntity cgp where cgp.id.groupId = :groupId")
    void deleteByGroupId(Long groupId);
}
