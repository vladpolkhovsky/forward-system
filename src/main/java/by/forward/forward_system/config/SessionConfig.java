package by.forward.forward_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
//@EnableJdbcHttpSession(tableName = "forward_system.SPRING_SESSION", cleanupCron = "0 0 * * * *")
public class SessionConfig {

}
