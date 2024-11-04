package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.FastChatMemberDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LoadChatMembersQueryHandler implements QueryHandler<Long, FastChatMemberDto> {

    @Language("SQL")
    private final static String QUERY = """
        select cm.id as id, u.id as userId, u.username as username from forward_system.chat_members cm\s
        	inner join forward_system.users u on cm.user_id = u.id
        	where cm.chat_id = ?
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
    public RowMapper<FastChatMemberDto> getRowMapper() {
        return (rs, rowNum) -> {
            FastChatMemberDto dto = new FastChatMemberDto();

            dto.setUserId(rs.getLong("userId"));
            dto.setUsername(rs.getString("username"));

            return dto;
        };
    }
}
