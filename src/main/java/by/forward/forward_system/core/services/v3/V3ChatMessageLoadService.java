package by.forward.forward_system.core.services.v3;

import by.forward.forward_system.core.dto.messenger.v3.chat.V3ChatDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3MessageDto;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3MessageSearchCriteria;
import by.forward.forward_system.core.dto.messenger.v3.chat.V3NewMessageDto;
import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import by.forward.forward_system.core.jpa.repository.LastMessageRepository;
import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class V3ChatMessageLoadService {

    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final LastMessageRepository lastMessageRepository;

    public List<V3NewMessageDto> findNewMessagesForUserAfter(Long userId, Long chatId, LocalDateTime afterTime) {
        return lastMessageRepository.newMessageToUserAndForChatFetch(userId, chatId, afterTime).stream()
            .map(messageMapper::mapToNewMessage)
            .toList();
    }

    public Page<V3MessageDto> search(V3MessageSearchCriteria criteria, Pageable pageable) {
        Page<Long> paginationIds = messageRepository.searchIdsPagination(criteria.getChatId(), criteria.getAfterTime(), pageable);
        List<V3MessageDto> mappedMessages = messageRepository.loadChatMessagesByPaginationIds(paginationIds.getContent()).stream()
            .map(messageMapper::map)
            .toList();
        return new PageImpl<>(mappedMessages, paginationIds.getPageable(), paginationIds.getTotalElements());
    }

    public V3MessageDto findById(Long messageId) {
        return messageMapper.map(messageRepository.findByIdAndFetch(messageId));
    }
}
