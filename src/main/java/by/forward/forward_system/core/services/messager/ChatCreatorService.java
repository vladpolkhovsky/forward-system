package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.dto.messenger.v3.ChatCreationDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.TagEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.kafka.events.CreateChatEvent;
import by.forward.forward_system.core.kafka.producer.CreateChatProducer;
import by.forward.forward_system.core.mapper.ChatMapper;
import by.forward.forward_system.core.mapper.TagMapper;
import by.forward.forward_system.core.services.TagService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class ChatCreatorService {

    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final ChatRepository chatRepository;
    private final CreateChatProducer producer;
    private final MessageService messageService;
    private final TagService tagService;
    private final TagMapper tagMapper;

    public void createChatAsync(ChatCreationDto creationDto, String initMessage, boolean addAdminAutomatically) {
        producer.publish(CreateChatEvent.builder()
            .dto(creationDto)
            .initialMessage(initMessage)
            .addAdminAutomatically(addAdminAutomatically)
            .build());
    }

    @Transactional
    public ChatEntity createChat(ChatCreationDto creationDto, String initMessage) {
        return createChat(creationDto, initMessage, true);
    }

    @Transactional
    public ChatEntity createChat(ChatCreationDto creationDto, String initMessage, boolean addAdminAutomatically) {
        try {
            log.info("Starting chat creation for dto: {}", creationDto);

            ChatEntity chatEntity = chatMapper.mapToDtoForSave(creationDto);
            log.debug("Mapped to entity: {}", chatEntity);

            Set<Long> chatMembers = new HashSet<>(CollectionUtils.emptyIfNull(creationDto.getMembers()));
            if (addAdminAutomatically) {
                List<Long> adminIds = userRepository.findUserIdsWithRole(Authority.ADMIN.getAuthority());
                log.debug("Found {} admin users to add", adminIds.size());
                chatMembers.addAll(adminIds);
            }
            log.debug("Final chat members count: {}", chatMembers.size());

            if (CollectionUtils.isNotEmpty(creationDto.getTags())) {
                List<TagEntity> tags = tagService.crateMany(tagMapper.mapFromChatTagToTag(creationDto.getTags()));
                chatEntity.getTags().addAll(tags);
                log.debug("Added {} tags to chat", tags.size());
            }

            chatEntity.getParticipants().addAll(chatMembers.stream().map(UserEntity::of).toList());

            ChatEntity savedChat = chatRepository.save(chatEntity);
            log.info("Successfully saved chat with id: {}", savedChat.getId());

            if (StringUtils.isNotBlank(initMessage)) {
                try {
                    messageService.sendMessage(UserEntity.of(ChatNames.SYSTEM_USER_ID), savedChat, initMessage,
                        true, ChatMessageType.SYSTEM_MESSAGE.entity());
                    log.info("Successfully sent initial message to chat: {}", savedChat.getId());
                } catch (Exception e) {
                    log.error("Failed to send initial message for chat: {}, but chat was created. Error: {}",
                        savedChat.getId(), e.getMessage());
                    // Не бросаем исключение дальше - чат уже создан
                }
            }

            return savedChat;

        } catch (Exception e) {
            log.error("Critical error during chat creation for dto: {}", creationDto, e);
            throw e; // Пробрасываем для retry механизма Kafka
        }
    }
}
