package by.forward.forward_system.core.jpa.repository.search;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatNameSearchRepository extends JpaRepository<ChatEntity, Long> {

    @Query(nativeQuery = true, value = """
    SELECT
        t.id,
        t.chat_name AS name,
        GREATEST(
            ts_rank(t.tsvector_chat_name, sq.ws_query),
            ts_rank(t.tsvector_chat_name, sq.or_query)
        ) + COALESCE((
            SELECT SUM(ar.value::real)
            FROM json_each_text(:jsonAdditionalRank ::json) AS ar(key, value)
            WHERE ar.key::bigint IN (
                SELECT ctt.tag_id
                FROM forward_system.chat_to_tag ctt
                WHERE ctt.chat_id = t.id
            )
        ), 0) AS rank
    FROM forward_system.chats t
    INNER JOIN forward_system.chat_members cm 
        ON t.id = cm.chat_id 
        AND cm.user_id = :currentUserId
    CROSS JOIN LATERAL (
        SELECT 
            websearch_to_tsquery('pg_catalog.russian', :chatNameQuery) AS ws_query,
            to_tsquery('pg_catalog.russian', 
                array_to_string(regexp_split_to_array(:chatNameQuery, '\\W+'), ' | ')
            ) AS or_query
    ) AS sq
    WHERE t.type IN (:chatTypes) and GREATEST(
            ts_rank(t.tsvector_chat_name, sq.ws_query),
            ts_rank(t.tsvector_chat_name, sq.or_query)
        ) + COALESCE((
            SELECT SUM(ar.value::real)
            FROM json_each_text(:jsonAdditionalRank ::json) AS ar(key, value)
            WHERE ar.key::bigint IN (
                SELECT ctt.tag_id 
                FROM forward_system.chat_to_tag ctt 
                WHERE ctt.chat_id = t.id
            )
        ), 0) > 0.001
    ORDER BY rank DESC, t.last_message_date DESC
    """)
    Page<ChatNameProjection> findChatsByChatNameAndTagsQuery(
        @Param("currentUserId") Long currentUserId,
        @Param("jsonAdditionalRank") String jsonAdditionalRank,
        @Param("chatNameQuery") String chatNameQuery,
        @Param("chatTypes") List<String> chatTypes,
        Pageable pageable);

    @Query(nativeQuery = true, value = """
    SELECT
        t.id,
        t.chat_name as name,
        GREATEST(
            ts_rank(t.tsvector_chat_name, ws_query),
            ts_rank(t.tsvector_chat_name, or_query)
        ) AS rank
    FROM forward_system.chats t
    INNER JOIN forward_system.chat_members cm
        ON t.id = cm.chat_id 
        AND cm.user_id = :currentUserId
    CROSS JOIN LATERAL (
        SELECT 
            websearch_to_tsquery('pg_catalog.russian', :chatNameQuery) AS ws_query,
            to_tsquery('pg_catalog.russian', 
                array_to_string(regexp_split_to_array(:chatNameQuery, '\\W+'), ' | ')
            ) AS or_query
    ) AS sq
    WHERE (t.tsvector_chat_name @@ sq.ws_query
        OR t.tsvector_chat_name @@ sq.or_query)
        AND t.type IN (:chatTypes) AND GREATEST(
            ts_rank(t.tsvector_chat_name, ws_query),
            ts_rank(t.tsvector_chat_name, or_query)
        ) > 0.001
    ORDER BY rank DESC, t.last_message_date DESC""")
    Page<ChatNameProjection> findChatsByNameQuery(
        @Param("currentUserId") Long currentUserId,
        @Param("chatNameQuery") String chatNameQuery,
        @Param("chatTypes") List<String> chatTypes,
        Pageable pageable);

    @Query(nativeQuery = true, value = """
        SELECT t.id,
               t.chat_name as name,
               0 AS rank
        FROM forward_system.chats t
             INNER JOIN forward_system.chat_members cm on t.id = cm.chat_id and cm.user_id = :currentUserId
        WHERE t.type in :chatTypes
        ORDER BY t.last_message_date DESC""")
    Page<ChatNameProjection> findChats(Long currentUserId, List<String> chatTypes, Pageable pageable);

    interface ChatNameProjection {
        Long getId();
        String getName();
        Float getRank();
    }
}
