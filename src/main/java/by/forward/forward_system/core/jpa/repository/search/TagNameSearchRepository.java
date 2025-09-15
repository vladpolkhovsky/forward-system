package by.forward.forward_system.core.jpa.repository.search;

import by.forward.forward_system.core.jpa.model.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagNameSearchRepository extends JpaRepository<TagEntity, String> {

    @Query(nativeQuery = true, value = """
        select
            t.id,
            t."name",
            ts_rank(t.tsvector_tag_name, to_tsquery('pg_catalog.russian', :tagNameQuery)) as rank
        from
            forward_system.tags t
        where
            t.tsvector_tag_name @@ to_tsquery('pg_catalog.russian', :tagNameQuery)
        order by
            rank desc
        limit 100
        """)
    List<TagNameProjection> search(@Param("tagNameQuery") String tagNameQuery);

    interface TagNameProjection {
        Long getId();

        String getName();

        Float getRank();
    }
}
