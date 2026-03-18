package by.forward.forward_system.core.iternalnotification;

import by.forward.forward_system.core.iternalnotification.dto.SendNotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendNotificationListener {

    private final NotificationQueueService notificationQueueService;

    @Async
    @EventListener
    public void listen(SendNotificationMessageDto messageDto) {
        log.info("Received notification message: {}", messageDto.getTittle());
        
        // Enqueue notification for async processing
        notificationQueueService.enqueue(messageDto);
        
        log.debug("Notification enqueued. Queue size: {}", notificationQueueService.getQueueSize());
    }
}
