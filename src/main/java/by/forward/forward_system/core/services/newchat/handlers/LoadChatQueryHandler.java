package by.forward.forward_system.core.services.newchat.handlers;


import by.forward.forward_system.core.dto.messenger.fast.FastChatDto;
import by.forward.forward_system.core.dto.messenger.fast.LoadChatRequestDto;
import by.forward.forward_system.core.services.newchat.ChatTabToChatTypeService;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class LoadChatQueryHandler implements QueryHandler<LoadChatRequestDto, FastChatDto> {

    private final static String QUERY_IN = ":IN:";
    @Language("SQL")
    private final static String QUERY = """
        select c.id as id, c.chat_name as chatName, c.last_message_date as lastMessage, count(cmtu.id) as newMsgCount, count(cs.id) > 0 as saved from forward_system.chats c
        	inner join forward_system.chat_members cm on cm.chat_id = c.id
        	left join forward_system.chat_message_to_user cmtu on cmtu.chat_id = c.id and cmtu.user_id = ? and not cmtu.is_viewed
            left join forward_system.chat_saved_to_user cs on cs.chat_id = c.id and cs.user_id = cm.user_id
        	where (c.type = ? or (? and cs.id is not null)) and cm.user_id = ? :IN:
        	group by c.id
        	order by c.last_message_date desc
        	limit ?;
        """;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    private final ChatTabToChatTypeService tabToChatType;

    @Override
    public String getQuery(LoadChatRequestDto request) {
        if (CollectionUtils.isEmpty(request.getLoaded())) {
            return QUERY.replace(QUERY_IN, "");
        }
        String collect = Stream.generate(() -> "?").limit(request.getLoaded().size()).collect(Collectors.joining(","));
        return QUERY.replace(QUERY_IN, " and c.id not in (" + collect + ")");
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(LoadChatRequestDto request) {
        return ps -> {
            ps.setLong(1, request.getUserId());
            ps.setString(2, tabToChatType.getChatTypeByTab(request.getChatTab()));
            ps.setBoolean(3, "use-saved".equals(tabToChatType.getChatTypeByTab(request.getChatTab())));
            ps.setLong(4, request.getUserId());
            int index = 5;
            for (int t = 0; t < request.getLoaded().size(); index++, t++) {
                ps.setLong(index, request.getLoaded().get(t));
            }
            ps.setLong(index, request.getSize());
        };
    }

    @Override
    public RowMapper<FastChatDto> getRowMapper() {
        return (rs, rowNum) -> {
            FastChatDto fastChatDto = new FastChatDto();

            fastChatDto.setId(rs.getLong("id"));
            fastChatDto.setChatName(rs.getString("chatName"));
            fastChatDto.setNewMessageCount(rs.getLong("newMsgCount"));
            fastChatDto.setSaved(rs.getBoolean("saved"));
            fastChatDto.setLastMessageDate(rs.getTimestamp("lastMessage").toLocalDateTime().format(dateTimeFormatter));

            return fastChatDto;
        };
    }

}
