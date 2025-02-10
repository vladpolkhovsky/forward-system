package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.FastChatDto;
import by.forward.forward_system.core.dto.messenger.fast.SearchChatRequestDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class SearchChatQueryHandler implements QueryHandler<SearchChatRequestDto, FastChatDto> {

    @Language("SQL")
    private final static String QUERY = """
        select c.id as id, c.chat_name as chatName, c.last_message_date as lastMessage, count(cmtu.id) as newMsgCount from forward_system.chats c
        	inner join forward_system.chat_members cm on cm.chat_id = c.id
        	left join forward_system.chat_message_to_user cmtu on cmtu.chat_id = c.id and cmtu.user_id = ? and not cmtu.is_viewed
        	where c.type = ? and cm.user_id = ? :IN:
        	group by c.id
        	order by c.last_message_date desc
        	limit ?;
        """;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");

    @Override
    public String getQuery(SearchChatRequestDto request) {
        return "";
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(SearchChatRequestDto request) {
        return null;
    }

    @Override
    public RowMapper<FastChatDto> getRowMapper() {
        return new RowMapper<FastChatDto>() {
            @Override
            public FastChatDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                FastChatDto fastChatDto = new FastChatDto();

                fastChatDto.setId(rs.getInt("id"));
                fastChatDto.setChatName(rs.getString("chatName"));
                fastChatDto.setNewMessageCount(rs.getInt("newMsgCount"));
                fastChatDto.setLastMessageDate(rs.getTimestamp("lastMessage").toLocalDateTime().format(dateTimeFormatter));

                return fastChatDto;
            }
        };
    }
}
