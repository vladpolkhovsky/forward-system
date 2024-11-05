package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.LoadChatMessagesRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class LoadChatMessagesQueryHandler implements QueryHandler<LoadChatMessagesRequestDto, SimpleChatMessageDto> {

    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select distinct cm.id as id, cm.from_user_id as fromUserId, cm.is_system_message as isSystem, cm."content" as text, cm.created_at as date, cmtu.is_viewed as viewed, cm.is_hidden as isHidden from forward_system.chat_messages cm
            left join forward_system.chat_message_to_user cmtu on cm.id = cmtu.message_id and cmtu.user_id = ?
            where cm.chat_id = ? :IN:
            order by cm.created_at desc
            limit ?
        """;

    @Override
    public String getQuery(LoadChatMessagesRequestDto request) {
        if (CollectionUtils.isEmpty(request.getLoaded())) {
            return QUERY.replace(QUERY_IN, "");
        }
        String collect = Stream.generate(() -> "?").limit(request.getLoaded().size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, " and cm.id not in (" + collect + ")");
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(LoadChatMessagesRequestDto request) {
        return ps -> {
            ps.setLong(1, request.getUserId());
            ps.setLong(2, request.getChatId());
            int index = 3;
            for (int t = 0; t < request.getLoaded().size(); index++, t++) {
                ps.setLong(index, request.getLoaded().get(t));
            }
            ps.setLong(index, request.getSize());
        };
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");

    @Override
    public RowMapper<SimpleChatMessageDto> getRowMapper() {
        return (rs, rowNum) -> {
            SimpleChatMessageDto dto = new SimpleChatMessageDto();

            String isViewed = rs.getString("viewed");

            dto.setId(rs.getLong("id"));
            dto.setFromUserId(rs.getLong("fromUserId"));
            dto.setSystem(rs.getBoolean("isSystem"));
            dto.setText(rs.getString("text"));
            dto.setDate(rs.getTimestamp("date").toLocalDateTime().format(dateTimeFormatter));
            dto.setViewed(isViewed == null ? true : BooleanUtils.toBoolean(isViewed));
            dto.setHidden(rs.getBoolean("isHidden"));

            return dto;
        };
    }

}
