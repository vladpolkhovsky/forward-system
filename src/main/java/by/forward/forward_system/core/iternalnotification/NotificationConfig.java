package by.forward.forward_system.core.iternalnotification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.notification.server")
@Getter
@Setter
public class NotificationConfig {

    private String url = "http://localhost:8085";
    private String login = "admin";
    private String password = "admin";
    private int tokenRefreshIntervalMinutes = 30;
    private int maxParallelRequests = 5;
}
