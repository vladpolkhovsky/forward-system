package by.forward.forward_system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import by.forward.forward_system.core.dto.auth.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtRelatedThings jwtRelatedThings;
    private final JwtRelatedThings.JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<Cookie> jwtCookie = Optional.ofNullable(request.getCookies()).map(Arrays::stream).stream()
            .flatMap(Function.identity())
            .filter(cookie -> cookie.getName().equals(jwtProperties.getJwtCookieName()))
            .findAny();

        jwtCookie.ifPresent(cookie -> {
            UserDto userDto = jwtRelatedThings.parseJwtToken(cookie.getValue());

            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDto,
                null,
                List.of()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            emptyContext.setAuthentication(authToken);

            SecurityContextHolder.setContext(emptyContext);
        });

        filterChain.doFilter(request, response);
    }
}
