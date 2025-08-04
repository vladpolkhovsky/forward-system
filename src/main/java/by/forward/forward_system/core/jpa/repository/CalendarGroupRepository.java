package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.CalendarGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarGroupRepository extends JpaRepository<CalendarGroupEntity, Long> {

}
