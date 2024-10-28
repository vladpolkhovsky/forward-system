package by.forward.forward_system.jobs;

import by.forward.forward_system.core.jpa.repository.ChatMessageToUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class DeleteOldMessageToUserJob {

    private final ChatMessageToUserRepository chatMessageToUserRepository;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public void deleteOldMessagesToUser() {
        LocalDateTime startTime = LocalDateTime.now();

        List<Long> allViewedOlderThen = chatMessageToUserRepository.getAllViewedOlderThen(startTime.minusDays(1));
        chatMessageToUserRepository.deleteAllById(allViewedOlderThen);

        long execTime = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        log.info("Удаление устаревших данных chat_message_to_user успешно завершено. Заняло {} сек. Удалено {} записей.", execTime, allViewedOlderThen.size());
    }
}
