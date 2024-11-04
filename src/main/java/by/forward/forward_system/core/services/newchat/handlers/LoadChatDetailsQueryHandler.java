package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.LoadChatInfoRequestDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatInfoDto;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LoadChatDetailsQueryHandler implements QueryHandler<LoadChatInfoRequestDto, SimpleChatInfoDto> {

    @Language("SQL")
    private final static String QUERY = """
            select c.id as id, c.chat_name as chatName, c.order_id as orderId, c.type as type, cm.owner_type_permission as onlyOwner from forward_system.chats c
            	left join forward_system.chat_metadata cm on c.id = cm.id
            	where c.id = ?
            """;

    @Override
    public String getQuery(LoadChatInfoRequestDto request) {
        return QUERY;
    }

    @Override
    public PreparedStatementSetter getPreparedStatementSetter(LoadChatInfoRequestDto request) {
        return ps -> {
            ps.setLong(1, request.getChatId());
        };
    }

    @Override
    public RowMapper<SimpleChatInfoDto> getRowMapper() {
        return (rs, rowNum) -> {
            SimpleChatInfoDto dto = new SimpleChatInfoDto();

            String orderId = rs.getString("orderId");
            String onlyOwner = rs.getString("onlyOwner");

            dto.setId(rs.getLong("id"));
            dto.setName(rs.getString("chatName"));
            dto.setOrderId(orderId == null ? null : Long.valueOf(orderId));
            dto.setChatType(rs.getString("type"));
            dto.setOnlyOwnerCanType(onlyOwner != null && BooleanUtils.toBoolean(onlyOwner));

            return dto;
        };
    }
}
