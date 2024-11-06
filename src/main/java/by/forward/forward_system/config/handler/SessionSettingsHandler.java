package by.forward.forward_system.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionSettingsHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final int MAX_INACTIVE_INTERVAL = 2629800;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        super.onAuthenticationSuccess(request, response, authentication);
        request.getSession().setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
    }
}
