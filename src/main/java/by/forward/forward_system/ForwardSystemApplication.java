package by.forward.forward_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class ForwardSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForwardSystemApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(@Autowired PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println(passwordEncoder.encode("9BGyVGoU2dhj"));
        };
    }

}
