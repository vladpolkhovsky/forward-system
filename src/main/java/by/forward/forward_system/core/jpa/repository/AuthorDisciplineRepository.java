package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.AuthorDisciplineEntity;
import by.forward.forward_system.core.jpa.model.DisciplineEntity;
import by.forward.forward_system.core.jpa.repository.projections.DisciplineProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorDisciplineRepository extends JpaRepository<AuthorDisciplineEntity, Long> {

    @Query(nativeQuery = true, value = """
            select ad.id, ad.author_id, d.id as discipline_id, d."name" as discipline_name, ad.discipline_quality from forward_system.author_disciplines ad\s
        	    inner join forward_system.disciplines d on ad.discipline_id = d.id
        """)
    List<DisciplineProjection> getAllDisciplines();

    @Query("""
        select de from AuthorDisciplineEntity de
            join fetch de.discipline
            join fetch de.disciplineQuality
            where de.author.id = :userId
        """)
    List<AuthorDisciplineEntity> findUserDisciplinesByUserId(Long userId);
}
