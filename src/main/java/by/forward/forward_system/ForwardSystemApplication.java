package by.forward.forward_system;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.TimeZone;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableAsync
public class ForwardSystemApplication {

    public static void main(String[] args) {
        System.setProperty("RANDOM_UUID", UUID.randomUUID().toString());

        System.setProperty("user.timezone", "Europe/Moscow");
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));

        SpringApplication.run(ForwardSystemApplication.class, args);
    }

    @PostConstruct
    public void init() {

    }

    @EventListener(ContextClosedEvent.class)
    @SneakyThrows
    public void onContextClosedEvent(ContextClosedEvent contextClosedEvent) {
        new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {

            }
            System.exit(-1);
        }).start();
    }

    @Bean
    ApplicationRunner applicationRunner(@Autowired PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println(passwordEncoder.encode("admin"));
        };
    }
}
