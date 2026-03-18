package by.forward.forward_system.core.iternalnotification;

import by.forward.forward_system.core.iternalnotification.dto.SendNotificationMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class NotificationQueueService {

    private final NotificationConfig config;
    private final NotificationAuthService authService;
    private final WebClient webClient;
    
    private final BlockingQueue<SendNotificationMessageDto> notificationQueue;
    private final ExecutorService executorService;
    private final AtomicInteger activeRequests;
    private final Semaphore semaphore;

    public NotificationQueueService(NotificationConfig config, NotificationAuthService authService) {
        this.config = config;
        this.authService = authService;
        
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .codecs(clientCodecsConfigurer -> 
                        clientCodecsConfigurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        
        // Queue with unlimited capacity
        this.notificationQueue = new LinkedBlockingQueue<>();
        
        // Thread pool for processing queue
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "notification-queue-processor");
            t.setDaemon(true);
            return t;
        });
        
        this.activeRequests = new AtomicInteger(0);
        this.semaphore = new Semaphore(config.getMaxParallelRequests());
        
        // Start queue processor
        startQueueProcessor();
        
        log.info("NotificationQueueService initialized with max {} parallel requests", 
                config.getMaxParallelRequests());
    }

    private void startQueueProcessor() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SendNotificationMessageDto message = notificationQueue.poll(1, TimeUnit.SECONDS);
                    if (message != null) {
                        processMessage(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error processing notification queue: {}", e.getMessage(), e);
                }
            }
        });
    }

    public void enqueue(SendNotificationMessageDto message) {
        boolean added = notificationQueue.offer(message);
        if (added) {
            log.debug("Notification enqueued. Queue size: {}", notificationQueue.size());
        } else {
            log.warn("Failed to enqueue notification message");
        }
    }

    private void processMessage(SendNotificationMessageDto message) {
        try {
            // Acquire permit for max parallel requests
            semaphore.acquire();
            
            if (activeRequests.incrementAndGet() > config.getMaxParallelRequests()) {
                activeRequests.decrementAndGet();
                semaphore.release();
                // Re-queue if over limit
                notificationQueue.offer(message);
                return;
            }
            
            sendNotificationAsync(message)
                    .subscribeOn(Schedulers.boundedElastic())
                    .doFinally(signal -> {
                        activeRequests.decrementAndGet();
                        semaphore.release();
                    })
                    .subscribe(
                            response -> log.debug("Notification sent successfully: {}", message.getTittle()),
                            error -> log.error("Failed to send notification: {}", error.getMessage())
                    );
                    
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while processing notification: {}", e.getMessage());
        }
    }

    @Async
    public Mono<Void> sendNotificationAsync(SendNotificationMessageDto message) {
        return authService.getToken()
                .flatMap(token -> webClient.post()
                        .uri("/api/bot/notification/any")
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(message)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .timeout(Duration.ofSeconds(60))
                        .doOnSuccess(v -> log.info("Notification sent: {}", message.getTittle()))
                        .doOnError(error -> log.error("Failed to send notification: {}", error.getMessage()))
                )
                .onErrorResume(error -> {
                    log.error("Error sending notification: {}", error.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    public int getQueueSize() {
        return notificationQueue.size();
    }

    public int getActiveRequests() {
        return activeRequests.get();
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
