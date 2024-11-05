package by.forward.forward_system.core.services.newchat;

import by.forward.forward_system.core.dto.messenger.fast.*;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatInfoDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageAttachmentDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageDto;
import by.forward.forward_system.core.dto.messenger.fast.partitional.SimpleChatMessageOptionDto;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.services.newchat.handlers.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class FastChatService {

    private final JdbcTemplate jdbcTemplate;

    private final LoadChatQueryHandler loadChatQueryHandler;

    private final LoadAllChatsWithNameQueryHandler loadAllChatsWithNameQueryHandler;

    private final LoadChatByIdsQueryHandler loadChatByIdsQueryHandler;

    private final LoadChatMessagesQueryHandler loadChatMessagesQueryHandler;

    private final LoadChatMessageOptionsQueryHandler loadChatMessageOptionsQueryHandler;

    private final LoadChatMessageAttachmentsQueryHandler loadChatMessageAttachmentsQueryHandler;

    private final LoadChatDetailsQueryHandler loadChatDetailsQueryHandler;

    private final ChatTabToChatTypeService chatTabToChatTypeService;

    private final LoadChatOrderQueryHandler loadChatOrderQueryHandler;

    private final LoadChatOrderParticipantsQueryHandler loadChatOrderParticipantsQueryHandler;

    private final LoadChatMembersQueryHandler loadChatMembersQueryHandler;

    private final LoadNewMessageCountQueryHandler newMessageCountQueryHandler;

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
        searchedChatsQuery = searchedChatsQuery.stream()
            .filter(t -> t.getType().equals(chatTabToChatTypeService.getChatTypeByTab(loadChatByIdRequestDto.getChatTab())))
            .toList();

        return new LoadChatResponseDto(
            loadChatByIdRequestDto.getUserId(),
            searchedChatsQuery,
            loadChatByIdRequestDto.getChatTab(),
            searchedChatsQuery.size()
        );
    }

    public LoadChatInfoResponseDto loadChatInfo(LoadChatInfoRequestDto requestDto) {

        String loadChatDetailsQuery = loadChatDetailsQueryHandler.getQuery(requestDto);
        PreparedStatementSetter loadChatDetailsPreparedStatementSetter = loadChatDetailsQueryHandler.getPreparedStatementSetter(requestDto);
        RowMapper<SimpleChatInfoDto> loadChatDetailsRowMapper = loadChatDetailsQueryHandler.getRowMapper();

        SimpleChatInfoDto chatInfo = jdbcTemplate.query(loadChatDetailsQuery, loadChatDetailsPreparedStatementSetter, loadChatDetailsRowMapper).get(0);

        OrderInfoDto orderInfoDto = null;
        if (chatInfo.getOrderId() != null) {
            String loadChatOrderQuery = loadChatOrderQueryHandler.getQuery(chatInfo.getOrderId());
            PreparedStatementSetter loadChatOrderPreparedStatementSetter = loadChatOrderQueryHandler.getPreparedStatementSetter(chatInfo.getOrderId());
            RowMapper<OrderInfoDto> loadChatOrderRowMapper = loadChatOrderQueryHandler.getRowMapper();

            orderInfoDto = jdbcTemplate.query(loadChatOrderQuery, loadChatOrderPreparedStatementSetter, loadChatOrderRowMapper).get(0);

            String loadChatOrderParticipantsQuery = loadChatOrderParticipantsQueryHandler.getQuery(chatInfo.getOrderId());
            PreparedStatementSetter loadChatOrderParticipantsPreparedStatementSetter = loadChatOrderParticipantsQueryHandler.getPreparedStatementSetter(chatInfo.getOrderId());
            RowMapper<OrderParticipantDto> loadChatOrderParticipantsRowMapper = loadChatOrderParticipantsQueryHandler.getRowMapper();

            List<OrderParticipantDto> participants = jdbcTemplate.query(loadChatOrderParticipantsQuery, loadChatOrderParticipantsPreparedStatementSetter, loadChatOrderParticipantsRowMapper);

            orderInfoDto.setParticipants(participants);
        }

        String loadChatMembersQuery = loadChatMembersQueryHandler.getQuery(requestDto.getChatId());
        PreparedStatementSetter loadChatMembersPreparedStatementSetter = loadChatMembersQueryHandler.getPreparedStatementSetter(requestDto.getChatId());
        RowMapper<FastChatMemberDto> loadChatMembersRowMapper = loadChatMembersQueryHandler.getRowMapper();

        List<FastChatMemberDto> chatMembers = jdbcTemplate.query(loadChatMembersQuery, loadChatMembersPreparedStatementSetter, loadChatMembersRowMapper);

        LoadChatMessagesRequestDto loadChatMessagesRequestDto = new LoadChatMessagesRequestDto();
        loadChatMessagesRequestDto.setSize(requestDto.getSize());
        loadChatMessagesRequestDto.setLoaded(Collections.emptyList());
        loadChatMessagesRequestDto.setChatId(requestDto.getChatId());
        loadChatMessagesRequestDto.setUserId(requestDto.getUserId());

        LoadChatMessagesResponseDto messages = loadChatMessages(loadChatMessagesRequestDto);

        return new LoadChatInfoResponseDto(
            chatInfo.getId(),
            chatInfo.getName(),
            chatInfo.getOrderId(),
            orderInfoDto,
            chatMembers,
            new ChatMetadataDto(chatInfo.getChatType(), chatInfo.isOnlyOwnerCanType()),
            messages.getMessages(),
            messages.getSize()
        );
    }

    public LoadChatMessagesResponseDto loadChatMessages(LoadChatMessagesRequestDto requestDto) {
        String loadChatsMessagesQuery = loadChatMessagesQueryHandler.getQuery(requestDto);
        PreparedStatementSetter loadChatsMessagesPreparedStatementSetter = loadChatMessagesQueryHandler.getPreparedStatementSetter(requestDto);
        RowMapper<SimpleChatMessageDto> loadChatsMessagesRowMapper = loadChatMessagesQueryHandler.getRowMapper();

        List<SimpleChatMessageDto> messages = jdbcTemplate.query(loadChatsMessagesQuery, loadChatsMessagesPreparedStatementSetter, loadChatsMessagesRowMapper);
        List<Long> messagesIds = messages.stream().map(SimpleChatMessageDto::getId).toList();

        if (messagesIds.isEmpty()) {
            return new LoadChatMessagesResponseDto(Collections.emptyList(), 0);
        }

        String loadChatMessageAttachmentsQuery = loadChatMessageAttachmentsQueryHandler.getQuery(messagesIds);
        PreparedStatementSetter loadChatMessageAttachmentsPreparedStatementSetter = loadChatMessageAttachmentsQueryHandler.getPreparedStatementSetter(messagesIds);
        RowMapper<SimpleChatMessageAttachmentDto> loadChatMessageAttachmentsRowMapper = loadChatMessageAttachmentsQueryHandler.getRowMapper();

        String loadChatMessageOptionsQuery = loadChatMessageOptionsQueryHandler.getQuery(messagesIds);
        PreparedStatementSetter loadChatMessageOptionsPreparedStatementSetter = loadChatMessageOptionsQueryHandler.getPreparedStatementSetter(messagesIds);
        RowMapper<SimpleChatMessageOptionDto> loadChatMessageOptionsRowMapper = loadChatMessageOptionsQueryHandler.getRowMapper();

        List<SimpleChatMessageOptionDto> options = jdbcTemplate.query(loadChatMessageOptionsQuery, loadChatMessageOptionsPreparedStatementSetter, loadChatMessageOptionsRowMapper);
        List<SimpleChatMessageAttachmentDto> attachments = jdbcTemplate.query(loadChatMessageAttachmentsQuery, loadChatMessageAttachmentsPreparedStatementSetter, loadChatMessageAttachmentsRowMapper);

        Map<Long, List<SimpleChatMessageOptionDto>> messageIdToOptions = new HashMap<>();
        Map<Long, List<SimpleChatMessageAttachmentDto>> messageIdToAttachments = new HashMap<>();

        for (SimpleChatMessageOptionDto option : options) {
            messageIdToOptions.putIfAbsent(option.getMessageId(), new ArrayList<>());
            messageIdToOptions.get(option.getMessageId()).add(option);
        }

        for (SimpleChatMessageAttachmentDto attachment : attachments) {
            messageIdToAttachments.putIfAbsent(attachment.getMessageId(), new ArrayList<>());
            messageIdToAttachments.get(attachment.getMessageId()).add(attachment);
        }

        List<FastChatMessageDto> messageList = messages.stream().map(message -> {
            FastChatMessageDto fastChatMessageDto = new FastChatMessageDto();

            fastChatMessageDto.setId(message.getId());
            fastChatMessageDto.setText(message.getText());
            fastChatMessageDto.setFromUserId(message.getFromUserId());
            fastChatMessageDto.setViewed(message.isViewed());
            fastChatMessageDto.setCreatedAt(message.getDate());
            fastChatMessageDto.setSystemMessage(message.isSystem());

            fastChatMessageDto.setAttachments(emptyIfNull(messageIdToAttachments.get(message.getId())).stream().map(t ->
                new FastChatMessageAttachmentDto(t.getFilename(), t.getAttachmentId())
            ).toList());

            fastChatMessageDto.setOptions(emptyIfNull(messageIdToOptions.get(message.getId())).stream().map(t ->
                new FastChatMessageOptionDto(t.getName(), t.getUrl())
            ).toList());

            return fastChatMessageDto;
        }).toList();

        return new LoadChatMessagesResponseDto(
            messageList,
            messageList.size()
        );
    }

    private <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public NewMessageCountResponseDto loadNewMessageCount(NewMessageCountRequestDto requestDto) {

        String query = newMessageCountQueryHandler.getQuery(requestDto.getUserId());
        PreparedStatementSetter preparedStatementSetter = newMessageCountQueryHandler.getPreparedStatementSetter(requestDto.getUserId());
        RowMapper<Map<String, Integer>> rowMapper = newMessageCountQueryHandler.getRowMapper();

        List<Map<String, Integer>> result = jdbcTemplate.query(query, preparedStatementSetter, rowMapper);

        HashMap<String, Integer> map = new HashMap<>();
        for (Map<String, Integer> stringIntegerMap : result) {
            map.putAll(stringIntegerMap);
        }

        Function<Integer, Integer> mapNullToZero = (value) -> value == null ? 0 : value;

        return new NewMessageCountResponseDto(
            mapNullToZero.apply(map.get(ChatType.REQUEST_ORDER_CHAT.getName())),
            mapNullToZero.apply(map.get(ChatType.ADMIN_TALK_CHAT.getName())),
            mapNullToZero.apply(map.get(ChatType.ORDER_CHAT.getName())),
            mapNullToZero.apply(map.get(ChatType.SPECIAL_CHAT.getName()))
        );
    }
}
