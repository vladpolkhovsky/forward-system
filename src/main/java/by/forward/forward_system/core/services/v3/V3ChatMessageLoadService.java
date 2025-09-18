package by.forward.forward_system.core.services.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3MessageDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3MessageSearchCriteria;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3NewMessageDto;
import by.forward.forward_system.core.jpa.model.MessageViewStatusView;
import by.forward.forward_system.core.jpa.repository.LastMessageRepository;
import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.jpa.repository.MessageViewStatusRepository;
import by.forward.forward_system.core.mapper.MessageMapper;
import by.forward.forward_system.core.utils.AuthUtils;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class V3ChatMessageLoadService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final LastMessageRepository lastMessageRepository;
    private final MessageViewStatusRepository messageViewStatusRepository;

    @Transactional(readOnly = true)
    public List<V3NewMessageDto> findNewMessagesForUserAfter(Long userId, Long chatId, LocalDateTime afterTime) {
        return lastMessageRepository.newMessageToUserAndForChatFetch(userId, chatId, afterTime).stream()
            .map(messageMapper::mapToNewMessage)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<V3MessageDto> search(V3MessageSearchCriteria criteria, Pageable pageable) {
        if (Objects.equals(criteria.getChatId(), ChatNames.NEWS_CHAT_ID)) {
            pageable = PageRequest.of(pageable.getPageNumber(), 5);
        }

        String currentUserUsername = AuthUtils.getCurrentUserDetails()
            .getUsername();

        Page<Long> paginationIds = messageRepository.searchIdsPagination(criteria.getChatId(), criteria.getAfterTime(), pageable);
        Map<Long, Set<String>> viewedById = messageViewStatusRepository.findAllById(paginationIds).stream()
            .collect(Collectors.toMap(MessageViewStatusView::getMessageId, t -> Set.copyOf(t.getViewedByUsers())));

        List<V3MessageDto> mappedMessages = messageRepository.loadChatMessagesByPaginationIds(paginationIds.getContent()).stream()
            .map(messageMapper::map)
            .map(t -> t.withIsNewMessage(Optional.ofNullable(viewedById.get(t.getId()))
                .orElse(Set.of()).contains(currentUserUsername))
                .withMessageReadedByUsernames(Optional.ofNullable(viewedById.get(t.getId()))
                .orElse(Set.of())))
            .toList();

        return new PageImpl<>(mappedMessages, paginationIds.getPageable(), paginationIds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public V3MessageDto findById(Long messageId) {
        return messageMapper.map(messageRepository.findByIdAndFetch(messageId));
    }
}
