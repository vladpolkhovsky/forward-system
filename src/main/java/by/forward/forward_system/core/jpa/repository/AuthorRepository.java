package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.AuthorEntity;
import by.forward.forward_system.core.jpa.repository.projections.AuthorWithDisciplineProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    @Query(value = "select distinct a from AuthorEntity a join fetch a.user join fetch a.createdByUser where not a.user.deleted")
    List<AuthorEntity> getAuthorsFast();

    @Query(nativeQuery = true, value = "select u.id as id, u.username as username, d.id as disciplineId, d.\"name\" as discipline, ad.discipline_quality as disciplineQuality from forward_system.authors a" +
        "   inner join forward_system.users u on a.id = u.id" +
        "   inner join forward_system.author_disciplines ad on ad.author_id = u.id" +
        "   inner join forward_system.disciplines d on ad.discipline_id  = d.id" +
        "   where not u.is_deleted")
    List<AuthorWithDisciplineProjection> findAllWithDisciplines();
}
