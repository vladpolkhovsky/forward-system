package by.forward.forward_system.core.events.listeners;

import by.forward.forward_system.core.dto.ai.AiResponseDto;
import by.forward.forward_system.core.events.events.CheckMessageByAiEvent;
import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AiLogRepository;
import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.services.core.AIDetector;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AiMessageCheckListener {

    private final AIDetector aiDetector;

    private final BanService banService;

    private final MessageRepository messageRepository;
    private final BotNotificationService botNotificationService;
    private final AiLogRepository aiLogRepository;

    @EventListener(CheckMessageByAiEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void listen(CheckMessageByAiEvent event) {
        log.info("AI: Проверяем сообщение {}", event.messageId());
        Optional<ChatMessageEntity> messageOpt = messageRepository.findById(event.messageId());

        messageOpt.filter(Predicate.not(ChatMessageEntity::getIsSystemMessage))
            .ifPresent(this::checkMessage);
    }

    @Retryable
    public void checkMessage(ChatMessageEntity message) {
        String checkingSubject = message.getChat()
            .getChatName();

        Optional<String> username = Optional.ofNullable(message.getFromUser())
            .map(UserEntity::getUsername);

        Optional<Long> authorId = Optional.ofNullable(message.getFromUser())
            .map(UserEntity::getId);

        Optional<String> targetText = Optional.ofNullable(StringUtils.trimToNull(message.getContent()));

        checkDataByAi(message.getId(), username.orElse("Заказчик"), authorId, targetText, checkingSubject);
    }

    @Retryable
    public void checkDataByAi(Long messageId, String authorName, Optional<Long> authorIdOpt, Optional<String> targetText, String checkingSubject) {
        log.info("AI: Проверяем сообщение [id={}] от {}[id={}] text={} subject={}", messageId, authorName, authorIdOpt, targetText, checkingSubject);

        targetText.map(text -> aiDetector.isValidMessage(text, authorName, checkingSubject))
            .filter(Predicate.not(AIDetector.AICheckResult::isOk))
            .stream()
            .peek(aiCheckResult -> {
                if (authorIdOpt.isEmpty()) {
                    notifyIfUnknownAuthorId(authorName, targetText.get(), checkingSubject, aiCheckResult.aiLogId());
                }
            })
            .findFirst()
            .filter(t -> authorIdOpt.isPresent())
            .ifPresent(aiCheckFailureResult -> {
                String reasonString = formatBanReasonString(targetText.get(), List.of(aiCheckFailureResult.aiLogId()));
                log.info("AI: Сообщение [id={}] не прошло проверку. ai-log-id={}", messageId, aiCheckFailureResult.aiLogId());
                banService.ban(authorIdOpt.get(), reasonString, List.of(aiCheckFailureResult.aiLogId()));
            });
    }

    private void notifyIfUnknownAuthorId(String authorName, String targetText, String checkingSubject, long aiLogId) {
        aiLogRepository.findById(aiLogId).ifPresent(aiLog -> {
            AiResponseDto aiResponseDto = JsonUtils.mapAiResponse(aiLog.getResponseJson());
            botNotificationService.sendBotNotificationToAdmins("""
                %s нарушил правила общения в "%s"
                ---
                Текст:
                %s
                ---
                Описание нарушения:
                %s
                """.formatted(authorName, checkingSubject, targetText, aiResponseDto.getExplanation()));
        });
    }

    private String formatBanReasonString(String message, List<Long> logIds) {
        String aiLog = logIds.stream().map("<a href=\"/ai-log/%d\" target=\"_blank\">Лог проверки</a>"::formatted)
            .collect(Collectors.joining(", "));
        return """
            Сообщение пользователя: "%s"
            Лог проверки: %s
            Содержат данные, которые не прошли проверку.
            """.formatted(message, aiLog);
    }
}
