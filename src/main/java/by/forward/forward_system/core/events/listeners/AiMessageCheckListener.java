package by.forward.forward_system.core.events.listeners;

import by.forward.forward_system.core.events.events.CheckMessageByAiEventDto;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.services.core.AIDetector;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.BanService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class AiMessageCheckListener {

    private final UserRepository userRepository;

    @Qualifier("springAsyncTaskExecutor")
    private final ThreadPoolTaskExecutor springAsyncTaskExecutor;
    private final AttachmentService attachmentService;
    private final AIDetector aiDetector;
    private final ChatRepository chatRepository;
    private final BanService banService;

    @EventListener(CheckMessageByAiEventDto.class)
    public void listen(CheckMessageByAiEventDto event) {
        springAsyncTaskExecutor.submit(() -> {
            checkMessageByAi(event);
        });
    }

    private void checkMessageByAi(CheckMessageByAiEventDto event) {
        Optional<String> chatNameById = chatRepository.findChatNameById(event.getChatId());
        List<Long> attachmentIds = event.getAttachmentIds();

        String username = userRepository.findById(event.getUserId()).orElseThrow(() -> new RuntimeException("User not found")).getUsername();

        boolean aiApproved = true;
        List<Long> logIds = new ArrayList<>();

        if (event.getMessage().isPresent() && StringUtils.isNoneBlank(event.getMessage().get())) {
            AIDetector.AICheckResult checkResult = aiDetector.isValidMessage(event.getMessage().get(), username, chatNameById.orElse("<идентификатор чата не найден>"));
            if (!checkResult.isOk()) {
                logIds.add(checkResult.aiLogId());
            }
            aiApproved &= checkResult.isOk();
        }

        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            for (Long attachmentId : attachmentIds) {
                AIDetector.AICheckResult checkResult = aiDetector.isValidFile(username, attachmentId);
                if (!checkResult.isOk()) {
                    logIds.add(checkResult.aiLogId());
                }
                aiApproved &= checkResult.isOk();
            }
        }

        if (!aiApproved) {
            banService.ban(event.getUserId(), formatBanReasonString(event.getMessage().orElse("Сообщение без текста"), attachmentIds, logIds), logIds);
        }
    }

    private String formatBanReasonString(String message, List<Long> attachmentIds, List<Long> logIds) {
        String files = attachmentIds.stream().map(t -> "<a href=\"/load-file/%d\" target=\"_blank\">Файл</a>".formatted(t)).collect(Collectors.joining(", "));
        String aiLog = logIds.stream().map(t -> "<a href=\"/ai-log/%d\" target=\"_blank\">Лог проверки</a>".formatted(t)).collect(Collectors.joining(", "));
        return """
            Сообщение пользователя: "%s"
            Приложенные файлы: %s
            Лог проверки: %s
            Содержат данные, которые не прошли провреку.
            """.formatted(message, files, aiLog);
    }
}
