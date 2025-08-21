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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
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

        ChatEntity chatEntity = chatMapper.mapToDtoForSave(creationDto);

        Set<Long> chatMembers = new HashSet<>(CollectionUtils.emptyIfNull(creationDto.getMembers()));
        if (addAdminAutomatically) {
            chatMembers.addAll(userRepository.findUserIdsWithRole(Authority.ADMIN.getAuthority()));
        }

        if (CollectionUtils.isNotEmpty(creationDto.getTags())) {
            List<TagEntity> tags = tagService.crateMany(tagMapper.mapFromChatTagToTag(creationDto.getTags()));
            chatEntity.getTags().addAll(tags);
        }

        chatEntity.getParticipants().addAll(chatMembers.stream().map(UserEntity::of).toList());

        chatEntity = chatRepository.save(chatEntity);

        if (StringUtils.isNotBlank(initMessage)) {
            messageService.sendMessage(UserEntity.of(ChatNames.SYSTEM_USER_ID), chatEntity, initMessage,
                true, ChatMessageType.SYSTEM_MESSAGE.entity());
        }

        return chatEntity;
    }
}
