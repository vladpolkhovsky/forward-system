package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.LoadChatsStatusRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.LoadChatsStatusResponseDto;
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
public class LoadChatOrderStatusByIdsQueryHandler implements QueryHandler<LoadChatsStatusRequestDto, LoadChatsStatusResponseDto.ChatStatus> {

    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select c.id as id, o.order_status as status from forward_system.chats c
        	inner join forward_system.orders o on o.id = c.order_id
            where :IN:
        """;

    @Override
    public String getQuery(LoadChatsStatusRequestDto request) {
        String collect = Stream.generate(() -> "?").limit(request.getChatIds().size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, " c.id in (" + collect + ")");
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(LoadChatsStatusRequestDto request) {
        return ps -> {
            for (int t = 0; t < request.getChatIds().size(); t++) {
                ps.setLong(t + 1, request.getChatIds().get(t));
            }
        };
    }

    @Override
    public RowMapper<LoadChatsStatusResponseDto.ChatStatus> getRowMapper() {
        return (rs, rowNum) -> {
            LoadChatsStatusResponseDto.ChatStatus fastChatDto = new LoadChatsStatusResponseDto.ChatStatus();

            fastChatDto.setId(rs.getLong("id"));
            fastChatDto.setStatus(rs.getString("status"));

            return fastChatDto;
        };
    }
}
