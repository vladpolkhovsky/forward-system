package by.forward.forward_system.core.services.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatSearchCriteria;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository.ChatTypeToChatsWithNewMessageCount;
import by.forward.forward_system.core.jpa.repository.ChatRepository.NewMessageCountProjection;
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
import java.util.stream.Collectors;

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

        String chatNameString = StringUtils.trimToNull(RegExUtils.replaceAll(criteria.getChatNameQuery(), "[^a-zA-Zа-яА-ЯёЁ0-9]+", " "));
        String tegNameString = StringUtils.trimToNull(RegExUtils.replaceAll(criteria.getTagNameQuery(), "[^a-zA-Zа-яА-ЯёЁ0-9]+", " "));

        if (StringUtils.isNotBlank(tegNameString)) {
            tegNameString = StringUtils.lowerCase(tegNameString);
            tegNameString = StringUtils.capitalize(tegNameString);

            List<TagNameProjection> searchedTags = tagSearchRepository.search(tegNameString);

            if (!searchedTags.isEmpty()) {
                Map<String, Float> additionalRank = searchedTags.stream().collect(Collectors.toMap(t -> t.getId().toString(), TagNameProjection::getRank));
                String jsonAdditionalRank = objectMapper.writeValueAsString(additionalRank);
                String chatNameQuery = defaultIfBlank(trimToNull(criteria.getChatNameQuery()), criteria.getTagNameQuery());

                Page<ChatNameProjection> chatsByChatNameAndTagsQuery = chatNameSearchRepository
                    .findChatsByChatNameAndTagsQuery(AuthUtils.getCurrentUserId(), jsonAdditionalRank, chatNameQuery, types, pageable);

                return toV3ChatDtoPage(chatsByChatNameAndTagsQuery);
            }
        }

        if (StringUtils.isNotBlank(chatNameString)) {
            chatNameString = StringUtils.lowerCase(chatNameString);
            chatNameString = StringUtils.capitalize(chatNameString);

            Page<ChatNameProjection> chatsByChatNameQuery = chatNameSearchRepository
                .findChatsByNameQuery(AuthUtils.getCurrentUserId(), chatNameString, types, pageable);

            return toV3ChatDtoPage(chatsByChatNameQuery);
        }

        Page<ChatNameProjection> chats = chatNameSearchRepository
            .findChats(AuthUtils.getCurrentUserId(), types, pageable);

        return toV3ChatDtoPage(chats);
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

        List<V3ChatDto> mapped = chatMapper.matToV3ChatDtoMany(fetchedChats).stream()
            .sorted(Comparator.<V3ChatDto>comparingDouble(t -> byIdRank.get(t.getId()))
                .thenComparing(t -> byIdLastMessageDate.get(t.getId()), LocalDateTime::compareTo).reversed())
            .map(t -> t.withNewMessageCount(Optional.ofNullable(byIdNewMessageCount.get(t.getId())).orElse(0)))
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
