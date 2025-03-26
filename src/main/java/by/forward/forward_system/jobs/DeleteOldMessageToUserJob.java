package by.forward.forward_system.jobs;

import by.forward.forward_system.core.jpa.repository.ChatMessageToUserRepository;
import by.forward.forward_system.core.jpa.repository.LastMessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class DeleteOldMessageToUserJob {

    private final ChatMessageToUserRepository chatMessageToUserRepository;

    private final LastMessageRepository lastMessageRepository;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void deleteOldMessagesToUser() {
        LocalDateTime startTime = LocalDateTime.now();

        List<Long> allViewedOlderThen = chatMessageToUserRepository.getAllViewedOlderThen(startTime.minusDays(1));
        chatMessageToUserRepository.deleteAllById(allViewedOlderThen);

        long execTime = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        log.info("Удаление устаревших данных chat_message_to_user успешно завершено. Заняло {} сек. Удалено {} записей.", execTime, allViewedOlderThen.size());
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void deleteLastMessage() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        lastMessageRepository.deleteAllByCreatedAtBefore(startTime);
    }
}
