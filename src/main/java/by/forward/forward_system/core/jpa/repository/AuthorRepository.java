package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.jpa.model.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    @Query(value = "select distinct a from AuthorEntity a join fetch a.user join fetch a.createdByUser")
    Collection<AuthorEntity> getAuthorsFast();

}
