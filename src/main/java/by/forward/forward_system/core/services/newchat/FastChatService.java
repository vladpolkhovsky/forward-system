package by.forward.forward_system.core.services.newchat;

import by.forward.forward_system.core.dto.messenger.fast.*;
import by.forward.forward_system.core.services.newchat.handlers.LoadAllChatsWithNameQueryHandler;
import by.forward.forward_system.core.services.newchat.handlers.LoadChatByIdsQueryHandler;
import by.forward.forward_system.core.services.newchat.handlers.LoadChatQueryHandler;
import by.forward.forward_system.core.services.newchat.handlers.SearchChatQueryHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FastChatService {

    private final JdbcTemplate jdbcTemplate;

    private final LoadChatQueryHandler loadChatQueryHandler;

    private final LoadAllChatsWithNameQueryHandler loadAllChatsWithNameQueryHandler;

    private final LoadChatByIdsQueryHandler loadChatByIdsQueryHandler;

    public LoadChatResponseDto loadChats(LoadChatRequestDto loadChatRequestDto) {
        String query = loadChatQueryHandler.getQuery(loadChatRequestDto);
        PreparedStatementSetter preparedStatementSetter = loadChatQueryHandler.getPreparedStatementSetter(loadChatRequestDto);
        RowMapper<FastChatDto> rowMapper = loadChatQueryHandler.getRowMapper();

        List<FastChatDto> result = jdbcTemplate.query(query, preparedStatementSetter, rowMapper);

        return new LoadChatResponseDto(
            loadChatRequestDto.getUserId(),
            result,
            loadChatRequestDto.getChatTab(),
            result.size()
        );
    }

    public LoadChatResponseDto searchChats(SearchChatRequestDto searchChatRequestDto) {
        String loadAllQuery = loadAllChatsWithNameQueryHandler.getQuery(searchChatRequestDto);
        PreparedStatementSetter loadAllPreparedStatementSetter = loadAllChatsWithNameQueryHandler.getPreparedStatementSetter(searchChatRequestDto);
        RowMapper<ChatWithNameDto> loadAllRowMapper = loadAllChatsWithNameQueryHandler.getRowMapper();

        List<ChatWithNameDto> loadAllChatsQuery = jdbcTemplate.query(loadAllQuery, loadAllPreparedStatementSetter, loadAllRowMapper);

        List<Long> searchedChats = new ArrayList<>();
        for (ChatWithNameDto chatWithNameDto : loadAllChatsQuery) {
            if (StringUtils.containsIgnoreCase(chatWithNameDto.getChatName(), searchChatRequestDto.getChatName())) {
                searchedChats.add(chatWithNameDto.getChatId());
            }
        }

        List<FastChatDto> searchedChatsQuery = new ArrayList<>();
        if (!searchedChats.isEmpty()) {
            LoadChatByIdsDto loadChatByIdsDto = new LoadChatByIdsDto(searchChatRequestDto.getUserId(), searchedChats);

            String loadChatByIdsQuery = loadChatByIdsQueryHandler.getQuery(loadChatByIdsDto);
            PreparedStatementSetter loadChatByIdsPreparedStatementSetter = loadChatByIdsQueryHandler.getPreparedStatementSetter(loadChatByIdsDto);
            RowMapper<FastChatDto> loadChatByIdsRowMapper = loadChatByIdsQueryHandler.getRowMapper();

            searchedChatsQuery = jdbcTemplate.query(loadChatByIdsQuery, loadChatByIdsPreparedStatementSetter, loadChatByIdsRowMapper);
        }

        return new LoadChatResponseDto(
            searchChatRequestDto.getUserId(),
            searchedChatsQuery,
            searchChatRequestDto.getChatTab(),
            searchedChats.size()
        );
    }

    public LoadChatResponseDto loadChatById(LoadChatByIdRequestDto loadChatByIdRequestDto) {
        LoadChatByIdsDto loadChatByIdsDto = new LoadChatByIdsDto(loadChatByIdRequestDto.getUserId(), List.of(loadChatByIdRequestDto.getChatId()));

        String loadChatByIdsQuery = loadChatByIdsQueryHandler.getQuery(loadChatByIdsDto);
        PreparedStatementSetter loadChatByIdsPreparedStatementSetter = loadChatByIdsQueryHandler.getPreparedStatementSetter(loadChatByIdsDto);
        RowMapper<FastChatDto> loadChatByIdsRowMapper = loadChatByIdsQueryHandler.getRowMapper();

        List<FastChatDto> searchedChatsQuery = jdbcTemplate.query(loadChatByIdsQuery, loadChatByIdsPreparedStatementSetter, loadChatByIdsRowMapper);

        return new LoadChatResponseDto(
            loadChatByIdRequestDto.getUserId(),
            searchedChatsQuery,
            loadChatByIdRequestDto.getChatTab(),
            searchedChatsQuery.size()
        );
    }
}
