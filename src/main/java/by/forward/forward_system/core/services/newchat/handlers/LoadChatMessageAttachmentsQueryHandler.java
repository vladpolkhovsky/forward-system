package by.forward.forward_system.core.services.newchat.handlers;

import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageAttachmentDto;
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
public class LoadChatMessageAttachmentsQueryHandler implements QueryHandler<List<Long>, SimpleChatMessageAttachmentDto> {

    private final static String QUERY_IN = ":IN:";

    @Language("SQL")
    private final static String QUERY = """
        select cma.id as id, cma.message_id as messageId, a.filename as filename, a.id as attachmentId from forward_system.chat_message_attachments cma\s
                   	inner join forward_system.attachments a on cma.attachment_id = a.id
                   	where cma.message_id in :IN:
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
    public RowMapper<SimpleChatMessageAttachmentDto> getRowMapper() {
        return (rs, rowNum) -> {
            SimpleChatMessageAttachmentDto dto = new SimpleChatMessageAttachmentDto();

            dto.setId(rs.getLong("id"));
            dto.setMessageId(rs.getLong("messageId"));
            dto.setAttachmentId(rs.getLong("attachmentId"));
            dto.setFilename(rs.getString("filename"));

            return dto;
        };
    }
}
