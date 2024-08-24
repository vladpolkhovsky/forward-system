package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.DisciplineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DisciplineRepository extends JpaRepository<DisciplineEntity, Long> {

    @Query(value = "select max(d.id) + 1 from DisciplineEntity d")
    Long nextDisciplineId();

}
