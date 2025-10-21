package by.forward.forward_system.config;

import java.util.Optional;
import by.forward.forward_system.core.dto.auth.UserDto;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, JwtRelatedThings jwtRelatedThings, UserRepository userRepository, UserMapper userMapper) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(t -> t
                .requestMatchers(
                    "/ws",
                    "/static/**",
                    "/api/new-chat/**",
                    "/api/messenger/file-save-form",
                    "/load-file/**",
                    "/emergency/**",
                    "/download-file/**",
                    "/api/v3/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .logout(t -> t.logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    HttpHeaders logoutHeaders = jwtRelatedThings.createLogoutHeaders();
                    logoutHeaders.get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
                        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    });
                    response.sendRedirect("/login");
                })
                .permitAll())
            .formLogin(t -> t.loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    UserDetails cud = (UserDetails) authentication.getPrincipal();
                    UserEntity byUsername = userRepository.findByUsernameAndDeletedIsFalse(cud.getUsername())
                        .orElseThrow();

                    UserDto userDto = userMapper.mapToAuthDto(byUsername);

                    HttpHeaders logoutHeaders = jwtRelatedThings.createSuccessfulSignInHeaders(userDto);
                    logoutHeaders.get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
                        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    });

                    response.sendRedirect("/main");
                })
                .permitAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS));;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
