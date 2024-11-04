package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageOptionDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class LoadChatMessageOptionsQueryHandler implements QueryHandler<List<Long>, SimpleChatMessageOptionDto> {
    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select cmo.id as id, cmo.message_id as messageId, cmo."content" as url, cmo.option_name as name from forward_system.chat_message_options cmo\s
            where cmo.message_id in :IN:
        """;

    @Override
    public String getQuery(List<Long> request) {
        String collect = Stream.generate(() -> "?").limit(request.size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, "(" + collect + ")");
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(List<Long> request) {
        return (ps -> {
            for (int i = 0; i < request.size(); i++) {
                ps.setLong(i + 1, request.get(i));
            }
        });
    }

    @Override
    public RowMapper<SimpleChatMessageOptionDto> getRowMapper() {
        return (rs, rowNum) -> {
            SimpleChatMessageOptionDto dto = new SimpleChatMessageOptionDto();

            dto.setId(rs.getLong("id"));
            dto.setMessageId(rs.getLong("messageId"));
            dto.setName(rs.getString("name"));
            dto.setUrl(rs.getString("url"));

            return dto;
        };
    }
}
