package by.forward.forward_system.core.auth;

import by.forward.forward_system.config.JwtRelatedThings;
import by.forward.forward_system.core.dto.auth.UserDto;
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
public class UserAgreementFilter implements Filter {

    private final JwtRelatedThings jwtRelatedThings;
    private final UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/user-agreement") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/login") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/logout") ||
            ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/static")) {
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
            if (byUsername.isPresent() && !byUsername.get().getNewAgreementSigned()) {
                redirectToSignPage((HttpServletResponse) servletResponse);
                return;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @SneakyThrows
    private void redirectToSignPage(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect("/user-agreement");
    }

    @SneakyThrows
    private void redirectToLogin(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect("/login");
    }
}
