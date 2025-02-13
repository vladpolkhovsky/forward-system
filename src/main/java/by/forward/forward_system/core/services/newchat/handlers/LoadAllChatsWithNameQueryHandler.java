package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.ChatWithNameDto;
import by.forward.forward_system.core.dto.messenger.fast.SearchChatRequestDto;
import by.forward.forward_system.core.services.newchat.ChatTabToChatTypeService;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LoadAllChatsWithNameQueryHandler implements QueryHandler<SearchChatRequestDto, ChatWithNameDto> {

    @Language("SQL")
    private final static String QUERY = """
        select c.id as id, c.chat_name as chatName from forward_system.chats c
            inner join forward_system.chat_members cm on c.id = cm.chat_id and cm.user_id = ?
            left join forward_system.chat_saved_to_user cs on cs.chat_id = c.id and cs.user_id = cm.user_id
            where (c.type = ? or (? and cs.id is not null));
        """;

    private final ChatTabToChatTypeService tabToChatType;

    @Override
    public String getQuery(SearchChatRequestDto request) {
        return QUERY;
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(SearchChatRequestDto request) {
        return ps -> {
            ps.setLong(1, request.getUserId());
            ps.setString(2, tabToChatType.getChatTypeByTab(request.getChatTab()));
            ps.setBoolean(3, "use-saved".equals(tabToChatType.getChatTypeByTab(request.getChatTab())));
        };
    }

    @Override
    public RowMapper<ChatWithNameDto> getRowMapper() {
        return (rs, rowNum) -> {
            ChatWithNameDto chatWithNameDto = new ChatWithNameDto();

            chatWithNameDto.setChatId(rs.getLong("id"));
            chatWithNameDto.setChatName(rs.getString("chatName"));

            return chatWithNameDto;
        };
    }
}
