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
        """;

    @Override
    public String getQuery(Long request) {
        return QUERY;
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(Long request) {
        return ps -> {
            ps.setLong(1, request);
        };
    }

    @Override
    public RowMapper<Map<String, Integer>> getRowMapper() {
        return (rs, rowNum) -> Map.of(rs.getString("type"), rs.getInt("count"));
    }
}
