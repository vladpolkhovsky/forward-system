package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.jpa.model.NotificationDataEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.NotificationDataRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

@Service
public class WebPushNotification {

    @Value("${vapid.private.key}")
    private String privateKey;

    @Getter
    @Value("${vapid.public.key}")
    private String publicKey;

    private PushService pushService;

    @Autowired
    private NotificationDataRepository notificationDataRepository;

    @Autowired
    private UserRepository userRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    private void inti() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey);
    }

    @SneakyThrows
    public void subscribe(Long userId, Long notificationId, Subscription subscription) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        String json = MAPPER.writeValueAsString(subscription);

        NotificationDataEntity notificationDataEntity = new NotificationDataEntity();
        notificationDataEntity.setUser(userEntity);
        notificationDataEntity.setSubscriptionData(json);
        notificationDataEntity.setUniqueNumber(notificationId);

        notificationDataRepository.save(notificationDataEntity);

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
        List<NotificationDataEntity> notificationDataEntities = notificationDataRepository.loadAllSubscriptions(userId);
        for (NotificationDataEntity notificationData : notificationDataEntities) {
            Subscription subscription = MAPPER.readValue(notificationData.getSubscriptionData(), Subscription.class);
            pushService.send(new Notification(subscription, json));
        }
    }

    public Boolean isSubscribed(Long currentUserId, Long notificationId) {
        return notificationDataRepository.isSubscribed(currentUserId, notificationId);
    }
}
