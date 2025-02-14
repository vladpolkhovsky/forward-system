package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class LoadNewMessageCountQueryHandler implements QueryHandler<Long, Map<String, Integer>> {

    @Language("SQL")
    private final static String QUERY = """
        select c."type" as type, count(distinct c.id) as count from forward_system.chat_message_to_user cmtu
        	inner join forward_system.chats c on c.id = cmtu.chat_id
        	where cmtu.is_viewed = false and cmtu.user_id = ?
        	group by c."type"
        union select 'saved' as type, count(distinct c.id) as count from forward_system.chat_saved_to_user cstu
        	inner join forward_system.chats c on c.id = cstu.chat_id
            inner join forward_system.chat_message_to_user cmtu on cmtu.chat_id = c.id
        	where cmtu.is_viewed = false and cmtu.user_id = ? and cstu.user_id = ?
        """;

    @Override
    public String getQuery(Long request) {
        return QUERY;
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(Long request) {
        return ps -> {
            ps.setLong(1, request);
            ps.setLong(2, request);
            ps.setLong(3, request);
        };
    }

    @Override
    public RowMapper<Map<String, Integer>> getRowMapper() {
        return (rs, rowNum) -> Map.of(rs.getString("type"), rs.getInt("count"));
    }
}
