package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.LoadIsChatMemberRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.LoadIsChatMemberResponseDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class LoadIsChatOrderMemberByIdsQueryHandler implements QueryHandler<LoadIsChatMemberRequestDto, LoadIsChatMemberResponseDto.IsChatMember> {

    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select c.id as id, op.id as userId from forward_system.chats c
        	inner join forward_system.orders o on o.id = c.order_id
            inner join forward_system.order_participants op on op.order_id = o.id
            where :IN: and op.user_id = ?
        """;

    @Override
    public String getQuery(LoadIsChatMemberRequestDto request) {
        String collect = Stream.generate(() -> "?").limit(request.getChatIds().size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, " c.id in (" + collect + ")");
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(LoadIsChatMemberRequestDto request) {
        return ps -> {
            for (int t = 0; t < request.getChatIds().size(); t++) {
                ps.setLong(t + 1, request.getChatIds().get(t));
            }
            ps.setLong(request.getChatIds().size() + 1, request.getUserId());
        };
    }

    @Override
    public RowMapper<LoadIsChatMemberResponseDto.IsChatMember> getRowMapper() {
        return (rs, rowNum) -> {
            LoadIsChatMemberResponseDto.IsChatMember isChatMember = new LoadIsChatMemberResponseDto.IsChatMember();

            isChatMember.setId(rs.getLong("id"));
            isChatMember.setIsMember(true);

            return isChatMember;
        };
    }
}
