package by.forward.forward_system.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final SavedRequestAwareAuthenticationSuccessHandler sessionSettingsHandler;

//    @Bean
//    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
//        UserDetails userDetails = User.withUsername("admin")
//            .authorities(Authority.OWNER)
//            .password(passwordEncoder().encode("admin"))
//            .build();
//        return new InMemoryUserDetailsManager(userDetails);
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(t -> t
                .requestMatchers("/ws", "/static/**", "/api/new-chat/**").permitAll()
                .anyRequest().authenticated()
            )
            .logout(t -> t.logoutUrl("/logout").permitAll())
            .formLogin(t -> t.loginPage("/login").defaultSuccessUrl("/main").successHandler(sessionSettingsHandler).permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
