package by.forward.forward_system.core.services.messager;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebPushNotification {

    @Value("${vapid.private.key}")
    private String privateKey;

    @Getter
    @Value("${vapid.public.key}")
    private String publicKey;

    private PushService pushService;

    private Map<Long, Subscription> subscriptions = new HashMap<>();

    @PostConstruct
    private void inti() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey);
    }

    public void subscribe(Long userId, Long notificationId, Subscription subscription) {
        subscriptions.put(userId, subscription);

        sendNotification(userId, "Первое сообщение!", "Вы подписались на рассылку сообщений");
    }

    @SneakyThrows
    public void sendNotification(Long userId, String messageTittle, String messageContent) {
        String json = """
            {
                "title": "%s",
                "body": "%s"
            }
            """.formatted(messageTittle, messageContent);
        Subscription subscription = subscriptions.get(userId);
        pushService.send(new Notification(subscription, json));
    }

    public Boolean isSubscribed(Long currentUserId, Long notificationId) {
        return subscriptions.containsKey(currentUserId);
    }
}
