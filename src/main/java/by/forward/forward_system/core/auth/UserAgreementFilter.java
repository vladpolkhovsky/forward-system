package by.forward.forward_system.core.auth;

import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserAgreementFilter implements Filter {

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
            HttpSession session = request.getSession(false);
            SecurityContextImpl sci = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");

            if (sci != null) {
                UserDetails cud = (UserDetails) sci.getAuthentication().getPrincipal();
                Optional<UserEntity> byUsername = userRepository.findByUsernameAndDeletedIsFalse(cud.getUsername());
                if (byUsername.isPresent() && byUsername.get().getNewAgreementSigned()) {
                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                } else {
                    redirectToSignPage((HttpServletResponse) servletResponse);
                    return;
                }
            }
        } catch (Exception ex) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @SneakyThrows
    private void redirectToSignPage(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect("/user-agreement");
    }
}
