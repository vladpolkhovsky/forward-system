package by.forward.forward_system.core.auth;

import by.forward.forward_system.config.JwtRelatedThings;
import by.forward.forward_system.core.dto.auth.UserDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class BanFilter implements Filter {

    private final UserRepository userRepository;
    private final JwtRelatedThings jwtRelatedThings;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/logout") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/login") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/ban") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/static/") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/download-file/") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/load-file/") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/ai-log/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            Optional<Cookie> jwtToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                    .filter(cookie -> Objects.equals(cookie.getName(), "JWT_TOKEN"))
                    .findAny();

            if (jwtToken.isEmpty()) {
                redirectToLogin((HttpServletResponse) servletResponse);
                return;
            }

            UserDto userDto = jwtRelatedThings.parseJwtToken(jwtToken.get().getValue());
            Optional<UserEntity> byUsername = userRepository.findByUsernameAndDeletedIsFalse(userDto.getUsername());

            if (byUsername.isPresent() && (byUsername.get().hasAuthority(Authority.BANNED) || byUsername.get().getDeleted())) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.sendRedirect("/ban");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @SneakyThrows
    private void redirectToLogin(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect("/login");
    }
}
