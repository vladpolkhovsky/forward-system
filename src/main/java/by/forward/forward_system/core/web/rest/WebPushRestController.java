package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.WebPushNotification;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import nl.martijndwars.webpush.Subscription;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/push")
@AllArgsConstructor
public class WebPushRestController {

    private final WebPushNotification webPushNotification;

    private final UserUiService userUiService;

    private final BotNotificationService botNotificationService;

    @GetMapping(value = "/get-subscription-status/{notificationId}")
    private ResponseEntity<SubscriptionResult> subscriptionStatus(@PathVariable Long notificationId) {
        Long currentUserId = userUiService.getCurrentUserId();
        return ResponseEntity.ok(new SubscriptionResult(webPushNotification.isSubscribed(currentUserId, notificationId)));
    }

    @GetMapping(value = "/public-key")
    private ResponseEntity<PublicKey> getPublicKey() {
        return ResponseEntity.ok(new PublicKey(webPushNotification.getPublicKey()));
    }

    @PostMapping(value = "/subscribe", consumes = MediaType.ALL_VALUE)
    private ResponseEntity<SubscriptionResult> subscribe(@RequestBody SubscriptionRequest subscriptionRequest) {
        webPushNotification.subscribe(userUiService.getCurrentUserId(), subscriptionRequest.notificationId(),  subscriptionRequest.subscription());
        return ResponseEntity.ok(new SubscriptionResult(true));
    }

    @GetMapping(value = "/get-bot-code", consumes = MediaType.ALL_VALUE)
    private ResponseEntity<BotCode> getBotCode() {
        return ResponseEntity.ok(new BotCode(botNotificationService.getCode(userUiService.getCurrentUserId())));
    }

    public record BotCode(String botCode) {

    }

    public record SubscriptionRequest(Long notificationId, Subscription subscription) {

    }

    public record PublicKey(String publicKey) {

    }

    private record SubscriptionResult(Boolean status) {

    }
}
