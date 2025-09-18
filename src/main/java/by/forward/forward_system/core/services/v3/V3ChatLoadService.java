package by.forward.forward_system.core.services.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatSearchCriteria;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository.ChatTypeToChatsWithNewMessageCount;
import by.forward.forward_system.core.jpa.repository.ChatRepository.NewMessageCountProjection;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository.ForwardOrderStatusProjection;
import by.forward.forward_system.core.jpa.repository.search.ChatNameSearchRepository;
import by.forward.forward_system.core.jpa.repository.search.ChatNameSearchRepository.ChatNameProjection;
import by.forward.forward_system.core.jpa.repository.search.TagNameSearchRepository;
import by.forward.forward_system.core.jpa.repository.search.TagNameSearchRepository.TagNameProjection;
import by.forward.forward_system.core.mapper.ChatMapper;
import by.forward.forward_system.core.utils.AuthUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class V3ChatLoadService {

    private final TagNameSearchRepository tagSearchRepository;
    private final ChatNameSearchRepository chatNameSearchRepository;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final ForwardOrderRepository forwardOrderRepository;

    @SneakyThrows
    @Transactional(readOnly = true)
    public V3ChatDto findById(Long id) {
        return toV3ChatDto(id);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public Page<V3ChatDto> search(@RequestBody V3ChatSearchCriteria criteria, Pageable pageable) {
        List<String> types = Arrays.stream(ChatType.values()).map(ChatType::name).toList();
        if (CollectionUtils.isNotEmpty(criteria.getChatTypes())) {
            types = criteria.getChatTypes().stream().map(ChatType::name).toList();
        }

        String tagNameSearch = Optional.ofNullable(criteria.getTagNameQuery())
            .map(query -> RegExUtils.replaceAll(query,"[^a-zA-Zа-яА-ЯёЁ0-9]+", " "))
            .map(query -> RegExUtils.replaceAll(query," +", " "))
            .map(StringUtils::trimToNull)
            .stream()
            .flatMap(query -> Arrays.stream(StringUtils.split(query)))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" | "));

        String chatNameSearch = Optional.ofNullable(criteria.getTagNameQuery())
            .map(query -> RegExUtils.replaceAll(query,"[^a-zA-Zа-яА-ЯёЁ0-9]+", " "))
            .map(query -> RegExUtils.replaceAll(query," +", " "))
            .map(StringUtils::trimToNull)
            .stream()
            .flatMap(query -> Arrays.stream(StringUtils.split(query)))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" | "));

        if (StringUtils.isNotBlank(tagNameSearch) || StringUtils.isNotBlank(chatNameSearch)) {
            List<TagNameProjection> tagSearch = tagSearchRepository.search(tagNameSearch);
            if (tagSearch.size() > 0) {
                Page<ChatNameProjection> chatsByChatNameAndTagsQuery =
                    chatNameSearchRepository.findChatsByChatNameAndTagsQuery(
                        AuthUtils.getCurrentUserId(), tagNameSearch, types, pageable);
                return toV3ChatDtoPage(chatsByChatNameAndTagsQuery);
            }

            return toV3ChatDtoPage(chatNameSearchRepository.findChatsByNameQuery(
                AuthUtils.getCurrentUserId(), chatNameSearch, types, pageable));
        }

        return toV3ChatDtoPage(chatNameSearchRepository.findChats(
            AuthUtils.getCurrentUserId(), types, pageable));
    }

    private V3ChatDto toV3ChatDto(Long chatId) {
        List<ChatEntity> fetchedChats = chatRepository.fetchDataToChatResponse(List.of(chatId));
        ChatEntity chat = fetchedChats.getFirst();

        V3ChatDto v3ChatDto = chatMapper.matToV3ChatDto(chat);

        NewMessageCountProjection newMessages = chatRepository.findNewMessageCount(List.of(chatId), AuthUtils.getCurrentUserId()).stream()
            .filter(t -> Objects.equals(t.getChatId(), chatId))
            .findAny().orElse(NewMessageCountProjection.zero(chatId));

        return v3ChatDto.withNewMessageCount(newMessages.getNewMessageCount());
    }

    private Page<V3ChatDto> toV3ChatDtoPage(Page<ChatNameProjection> page) {
        List<ChatNameProjection> content = page.getContent();

        Map<Long, Float> byIdRank = content.stream()
            .collect(Collectors.toMap(ChatNameProjection::getId, ChatNameProjection::getRank));

        List<Long> ids = content.stream().map(ChatNameProjection::getId).toList();
        List<ChatEntity> fetchedChats = chatRepository.fetchDataToChatResponse(ids);

        Map<Long, Integer> byIdNewMessageCount = chatRepository.findNewMessageCount(ids, AuthUtils.getCurrentUserId())
            .stream().collect(Collectors.toMap(NewMessageCountProjection::getChatId, NewMessageCountProjection::getNewMessageCount));

        Map<Long, LocalDateTime> byIdLastMessageDate = fetchedChats.stream()
            .collect(Collectors.toMap(ChatEntity::getId, ChatEntity::getLastMessageDate));

        Map<Long, ForwardOrderStatusProjection> byIdForwardOrderStatus = forwardOrderRepository.findByChatIds(ids).stream()
            .flatMap(t -> Stream.of(Map.entry(t.getChatId(), t), Map.entry(t.getAdminChatId(), t)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<V3ChatDto> mapped = chatMapper.matToV3ChatDtoMany(fetchedChats).stream()
            .sorted(Comparator.<V3ChatDto>comparingDouble(t -> byIdRank.get(t.getId()))
                .thenComparing(t -> byIdLastMessageDate.get(t.getId()), LocalDateTime::compareTo).reversed())
            .map(t -> t.withNewMessageCount(Optional.ofNullable(byIdNewMessageCount.get(t.getId())).orElse(0)))
            .map(t -> t.withIsForwardOrder(byIdForwardOrderStatus.containsKey(t.getId()))
                .withIsForwardOrderPaid(Optional.ofNullable(byIdForwardOrderStatus.get(t.getId())).map(ForwardOrderStatusProjection::getPaid).orElse(null)))
            .toList();

        return new PageImpl<>(mapped, page.getPageable(), page.getTotalElements());
    }

    public Map<String, Long> getNewMessageCount(Long userId) {
        var typeToCount = chatRepository.findChatTypeToChatsWithNewMessageCount(userId);
        var typeToCountMap = typeToCount.stream().collect(Collectors.toMap(ChatTypeToChatsWithNewMessageCount::getType, ChatTypeToChatsWithNewMessageCount::getCount));
        return Arrays.stream(ChatType.values())
            .collect(Collectors.toMap(ChatType::getName, t -> typeToCountMap.getOrDefault(t.getName(), 0).longValue()));
    }
}
