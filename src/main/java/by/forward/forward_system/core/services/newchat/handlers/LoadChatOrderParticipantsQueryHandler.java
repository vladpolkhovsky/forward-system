package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.OrderParticipantDto;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.services.newchat.QueryHandler;
import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LoadChatOrderParticipantsQueryHandler implements QueryHandler<Long, OrderParticipantDto> {

    @Language("SQL")
    private final static String QUERY = """
        select op.user_id as userId, op."type" as type from forward_system.order_participants op
        	where op.order_id = ?
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
    public RowMapper<OrderParticipantDto> getRowMapper() {
        return (rs, rowNum) -> {
            OrderParticipantDto dto = new OrderParticipantDto();

            dto.setType(ParticipantType.byName(rs.getString("type")).getName());
            dto.setUserId(rs.getLong("userId"));
            dto.setTypeRus(ParticipantType.byName(rs.getString("type")).getRusName());

            return dto;
        };
    }
}
