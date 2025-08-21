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
    SELECT 
        t.id,
        t.name,
        GREATEST(
            ts_rank(t.tsvector_tag_name, sq.ws_query),
            ts_rank(t.tsvector_tag_name, sq.or_query)
        ) AS rank
    FROM forward_system.tags t
    CROSS JOIN LATERAL (
        SELECT 
            websearch_to_tsquery('pg_catalog.russian', :tagNameQuery) AS ws_query,
            to_tsquery('pg_catalog.russian',
                array_to_string(regexp_split_to_array(:tagNameQuery, '\\W+'), ' | ')
            ) AS or_query
    ) AS sq
    WHERE (t.tsvector_tag_name @@ sq.ws_query
       OR t.tsvector_tag_name @@ sq.or_query) and GREATEST(
            ts_rank(t.tsvector_tag_name, sq.ws_query),
            ts_rank(t.tsvector_tag_name, sq.or_query)
        ) > 0.001
    ORDER BY rank DESC 
    LIMIT 100
    """)
    List<TagNameProjection> search(@Param("tagNameQuery") String tagNameQuery);

    interface TagNameProjection {
        Long getId();
        String getName();
        Float getRank();
    }
}
