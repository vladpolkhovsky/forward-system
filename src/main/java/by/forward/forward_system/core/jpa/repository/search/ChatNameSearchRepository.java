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
        select
        	c.id,
        	c.chat_name as name,
        	ts_rank((forward_system.tsvector_agg(distinct t.tsvector_tag_name) || c.tsvector_chat_name), to_tsquery('simple', :chatNameQuery)) as rank
        from forward_system.chats c
        inner join forward_system.chat_members cm on
            c.id = cm.chat_id and (cm.user_id = :currentUserId or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = cm.user_id
                          and op.order_id = c.order_id
                          and op.type = 'HOST'
                    )))
        left join forward_system.chat_to_tag ctt on
        	c.id = ctt.chat_id
        left join forward_system.tags t on
        	ctt.tag_id = t.id
        where
            c.type in :chatTypes or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = :currentUserId
                          and op.order_id = c.order_id
                          and op.type = 'SUB'
                    ))
        group by
        	c.id
        having
        	ts_rank((forward_system.tsvector_agg(distinct t.tsvector_tag_name) || c.tsvector_chat_name), to_tsquery('simple', :chatNameQuery)) > 0.03
        order by
        	rank desc, c.last_message_date desc
        """)
    Page<ChatNameProjection> findChatsByChatNameAndTagsQuery(
        @Param("currentUserId") Long currentUserId,
        @Param("chatNameQuery") String chatNameQuery,
        @Param("allowSubChat") Boolean allowSubChat,
        @Param("chatTypes") List<String> chatTypes,
        Pageable pageable);

    @Query(nativeQuery = true, value = """
        select
        	c.id,
        	c.chat_name as name,
        	ts_rank(c.tsvector_chat_name, to_tsquery('simple', :chatNameQuery)) as rank
        from forward_system.chats c
        inner join forward_system.chat_members cm on
            c.id = cm.chat_id and (cm.user_id = :currentUserId or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = cm.user_id
                          and op.order_id = c.order_id
                          and op.type = 'HOST'
                    )))
        where
            c.type in :chatTypes or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = :currentUserId
                          and op.order_id = c.order_id
                          and op.type = 'SUB'
                    ))
        group by
        	c.id
        having
        	ts_rank(c.tsvector_chat_name, to_tsquery('simple', :chatNameQuery)) > 0.03
        order by
        	rank desc, c.last_message_date desc
        """)
    Page<ChatNameProjection> findChatsByNameQuery(
        @Param("currentUserId") Long currentUserId,
        @Param("chatNameQuery") String chatNameQuery,
        @Param("allowSubChat") Boolean allowSubChat,
        @Param("chatTypes") List<String> chatTypes,
        Pageable pageable);

    @Query(nativeQuery = true, value = """
        select
           c.id,
           c.chat_name as name,
           0 as rank
        from forward_system.chats c
        inner join forward_system.chat_members cm on
            c.id = cm.chat_id and (cm.user_id = :currentUserId or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = cm.user_id
                          and op.order_id = c.order_id
                          and op.type = 'HOST'
                    )))
        where
            c.type in :chatTypes or (:allowSubChat and exists (
                    select 1 from forward_system.order_participants op
                        where op.user_id = :currentUserId
                          and op.order_id = c.order_id
                          and op.type = 'SUB'
                    ))
        order by
           rank desc, c.last_message_date desc
        """)
    Page<ChatNameProjection> findChats(Long currentUserId, Boolean allowSubChat, List<String> chatTypes, Pageable pageable);

    interface ChatNameProjection {
        Long getId();
        String getName();
        Float getRank();
    }
}
