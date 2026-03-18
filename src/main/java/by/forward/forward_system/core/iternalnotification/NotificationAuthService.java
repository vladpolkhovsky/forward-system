package by.forward.forward_system.core.iternalnotification;

import by.forward.forward_system.core.iternalnotification.dto.LoginRequest;
import by.forward.forward_system.core.iternalnotification.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class NotificationAuthService {

    private final NotificationConfig config;
    private final WebClient webClient;
    
    private volatile String currentToken;
    private volatile long tokenExpiresAt;

    public NotificationAuthService(NotificationConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .codecs(clientCodecsConfigurer -> 
                        clientCodecsConfigurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public Mono<String> getToken() {
        if (currentToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return Mono.just(currentToken);
        }
        return authenticate();
    }

    private synchronized Mono<String> authenticate() {
        // Double-check after acquiring lock
        if (currentToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return Mono.just(currentToken);
        }
        
        log.info("Authenticating with notification server: {}", config.getUrl());
        
        LoginRequest loginRequest = LoginRequest.builder()
                .login(config.getLogin())
                .password(config.getPassword())
                .build();

        return webClient.post()
                .uri("/api/bot/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(response -> {
                    this.currentToken = response.getToken();
                    long refreshInterval = config.getTokenRefreshIntervalMinutes() * 60 * 1000L;
                    // Refresh 5 minute before expiration
                    this.tokenExpiresAt = System.currentTimeMillis() + refreshInterval - 5 * 60000;
                    log.info("Successfully authenticated with notification server. Token will refresh at: {}", 
                            tokenExpiresAt);
                })
                .doOnError(error -> log.error("Failed to authenticate with notification server: {}", error.getMessage()))
                .map(LoginResponse::getToken)
                .onErrorResume(error -> {
                    log.error("Error during authentication: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    @Scheduled(fixedRateString = "#{${app.notification.server.token-refresh-interval-minutes:30} * 60 * 1000}")
    public void refreshTokenIfNeeded() {
        if (currentToken == null || System.currentTimeMillis() >= tokenExpiresAt) {
            log.info("Token refresh needed or token is null");
            authenticate().block();
        }
    }
    
    public void forceRefreshToken() {
        log.info("Forcing token refresh");
        authenticate().block();
    }
}
