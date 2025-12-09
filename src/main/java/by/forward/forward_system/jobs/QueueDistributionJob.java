package by.forward.forward_system.jobs;

import by.forward.forward_system.core.enums.DistributionItemStatusType;
import by.forward.forward_system.core.jpa.model.QueueDistributionEntity;
import by.forward.forward_system.core.jpa.model.QueueDistributionItemEntity;
import by.forward.forward_system.core.jpa.repository.QueueDistributionRepository;
import by.forward.forward_system.core.services.NewDistributionService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class QueueDistributionJob {

    private final QueueDistributionRepository queueDistributionRepository;
    private final NewDistributionService newDistributionService;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void plannedToInProgress() {
        List<QueueDistributionEntity> plannedAndShouldStart = queueDistributionRepository.findPlannedAndShouldStart(LocalDateTime.now());
        plannedAndShouldStart.forEach(newDistributionService::toInProgress);
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void process() {
        LocalDateTime beginAt = LocalDateTime.now();
        List<QueueDistributionEntity> inProgress = queueDistributionRepository.findInProgress();

        for (QueueDistributionEntity distribution : inProgress) {
            List<QueueDistributionItemEntity> items = distribution.getItems();
            items.sort(Comparator.comparing(QueueDistributionItemEntity::getDistributionOrder));

            boolean isNotStarted = items.stream()
                .allMatch(t -> t.getStatus() == DistributionItemStatusType.WAITING);

            if (isNotStarted) {
                if (items.isEmpty()) {
                    end(distribution);
                    continue;
                }
                QueueDistributionItemEntity triggerThis = items.getFirst();
                trigger(triggerThis);
                continue;
            }

            boolean shouldTriggerNextElement = false;
            boolean isAnyInProgress = false;

            for (QueueDistributionItemEntity item : items) {
                if (item.getStatus() == DistributionItemStatusType.NO_RESPONSE
                    || item.getStatus() == DistributionItemStatusType.SKIPPED
                    || item.getStatus() == DistributionItemStatusType.DECLINE) {
                    continue;
                }
                if (item.getStatus() == DistributionItemStatusType.TALK) {
                    if (beginAt.isAfter(item.getWaitStart().plusDays(2))) {
                        skip(item);
                    }
                    isAnyInProgress = true;
                }
                if (item.getStatus() == DistributionItemStatusType.IN_PROGRESS) {
                    if (beginAt.isAfter(item.getWaitUntil())) {
                        noResponse(item);
                        shouldTriggerNextElement = true;
                    }
                    isAnyInProgress = true;
                }
            }

            if (shouldTriggerNextElement || !isAnyInProgress) {
                Optional<QueueDistributionItemEntity> triggerThis = items.stream()
                    .filter(t -> t.getStatus() == DistributionItemStatusType.WAITING)
                    .findFirst();
                if (triggerThis.isEmpty()) {
                    end(distribution);
                    continue;
                }
                trigger(triggerThis.get());
            }
        }
    }

    private void noResponse(QueueDistributionItemEntity item) {
        newDistributionService.noResponseOnItem(item);
    }

    private void skip(QueueDistributionItemEntity item) {
        newDistributionService.skipBecauseWaitTooLong(item);
    }

    private void trigger(QueueDistributionItemEntity item) {
        newDistributionService.triggerNewItemToStart(item);
    }

    private void end(QueueDistributionEntity distribution) {
        newDistributionService.finishDistributionWithoutResult(distribution);
    }
}
