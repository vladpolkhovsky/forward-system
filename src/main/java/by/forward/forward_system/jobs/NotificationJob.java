package by.forward.forward_system.jobs;

import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.messager.WebPushNotification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class NotificationJob {

    private final ChatService chatService;

    private final WebPushNotification webPushNotification;

    //@Scheduled(fixedDelay = 90, timeUnit = TimeUnit.MINUTES)
    public void notifyNotViewedMessages() {
        List<Long> userIds = chatService.getUsersWithNotViewedMessaged();
        for (Long userId : userIds) {
            webPushNotification.sendNotification(userId, "Новое сообщение", "У вас есть непрочитанные сообщения.");
        }
    }
}
