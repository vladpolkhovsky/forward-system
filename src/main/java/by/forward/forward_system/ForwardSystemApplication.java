package by.forward.forward_system;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ForwardSystemApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ForwardSystemApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(@Autowired PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println(passwordEncoder.encode("admin"));
        };
    }

}
