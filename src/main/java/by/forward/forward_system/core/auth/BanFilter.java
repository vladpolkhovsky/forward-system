package by.forward.forward_system.core.auth;

import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BanFilter implements Filter {

    private final UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/logout")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpSession session = request.getSession(false);
            SecurityContextImpl sci = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");

            if (sci != null) {
                UserDetails cud = (UserDetails) sci.getAuthentication().getPrincipal();
                Optional<UserEntity> byUsername = userRepository.findByUsername(cud.getUsername());
                if (byUsername.isPresent() && byUsername.get().getAuthorities().contains(Authority.BANNED)) {
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.sendRedirect("/logout");
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}