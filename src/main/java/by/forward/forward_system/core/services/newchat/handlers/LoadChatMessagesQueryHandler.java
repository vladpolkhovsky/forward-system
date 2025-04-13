package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.LoadChatMessagesRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class LoadChatMessagesQueryHandler implements QueryHandler<LoadChatMessagesRequestDto, SimpleChatMessageDto> {

    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select distinct cm.id as id, cm.chat_id as chatId, c.chat_name as chatName, cm.from_user_id as fromUserId, u.username as fromUserUsername, cm.is_system_message as isSystem, cm."content" as text, cm.chat_message_type as messageType, cm.created_at as date, cmtu.is_viewed as viewed, cm.is_hidden as isHidden from forward_system.chat_messages cm
            left join forward_system.chats c on c.id = cm.chat_id
            left join forward_system.users u on u.id = cm.from_user_id
            left join forward_system.chat_message_to_user cmtu on cm.id = cmtu.message_id and cmtu.user_id = ?
            where cm.chat_id = ? :IN:
            order by cm.created_at desc
            limit ?
        """;

    @Language("SQL")
    private final String QUERY_BY_IDS = """
        select distinct cm.id as id, cm.chat_id as chatId, c.chat_name as chatName, cm.from_user_id as fromUserId, u.username as fromUserUsername, cm.is_system_message as isSystem, cm."content" as text, cm.chat_message_type as messageType, cm.created_at as date, cmtu.is_viewed as viewed, cm.is_hidden as isHidden from forward_system.chat_messages cm
            left join forward_system.chats c on c.id = cm.chat_id
            left join forward_system.users u on u.id = cm.from_user_id
            left join forward_system.chat_message_to_user cmtu on cm.id = cmtu.message_id and cmtu.user_id = ?
            where cm.id in :IN:
            order by cm.created_at asc
        """;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String getQuery(LoadChatMessagesRequestDto request) {
        if (CollectionUtils.isEmpty(request.getLoaded())) {
            return QUERY.replace(QUERY_IN, "");
        }
        String collect = Stream.generate(() -> "?").limit(request.getLoaded().size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, " and cm.id not in (" + collect + ")");
    }

    public List<SimpleChatMessageDto> getQueryByIds(List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return List.of();
        }

        PreparedStatementSetter statementSetter = ps -> {
            ps.setLong(1, userId);
            for (int i = 0; i < messageIds.size(); i++) {
                ps.setLong(i + 2, messageIds.get(i));
            }
        };

        String params = Stream.generate(() -> "?").limit(messageIds.size()).collect(Collectors.joining(","));

        String normalSql = QUERY_BY_IDS.replace(":IN:", "(" + params + ")");

        return jdbcTemplate.query(normalSql, statementSetter, getRowMapper());
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

    @Override
    public RowMapper<SimpleChatMessageDto> getRowMapper() {
        return (rs, rowNum) -> {
            SimpleChatMessageDto dto = new SimpleChatMessageDto();

            String isViewed = rs.getString("viewed");

            dto.setId(rs.getLong("id"));
            dto.setChatId(rs.getLong("chatId"));
            dto.setChatName(rs.getString("chatName"));
            dto.setFromUserId(rs.getLong("fromUserId"));
            dto.setFromUserUsername(rs.getString("fromUserUsername"));
            dto.setSystem(rs.getBoolean("isSystem"));
            dto.setText(rs.getString("text"));
            dto.setMessageType(rs.getString("messageType"));
            dto.setDate(formatDateToString(rs.getTimestamp("date").toLocalDateTime()));
            dto.setCreatedAtTimestamp(rs.getTimestamp("date").toInstant().toEpochMilli());
            dto.setViewed(isViewed == null ? true : BooleanUtils.toBoolean(isViewed));
            dto.setHidden(rs.getBoolean("isHidden"));

            return dto;
        };
    }

    private String formatDateToString(LocalDateTime date) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        if (date.isAfter(dayStart)) {
            return date.format(timeFormatter);
        }
        return date.format(dateTimeFormatter);
    }
}
