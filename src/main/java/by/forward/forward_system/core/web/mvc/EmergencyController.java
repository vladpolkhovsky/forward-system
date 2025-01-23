package by.forward.forward_system.core.web.mvc;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class EmergencyController {

    @Qualifier("emergencyMenuUrl")
    private String emergencyMenuUrl;

    @Qualifier("shutdownUrl")
    private String shutdownUrl;

    @Qualifier("secretPassword")
    private String secretPassword;

    @GetMapping("/emergency")
    public String emergencyMenuLogin() {
        return "main/emergency/emergency-menu-login";
    }

    @PostMapping("/emergency/password")
    public RedirectView emergencyPassword(@RequestParam("password") String password) {
        if (password.equals(secretPassword)) {
            return new RedirectView(emergencyMenuUrl);
        }
        return new RedirectView("/emergency?incorrect");
    }

    @GetMapping(path = "${management.emergency-menu-url}")
    public String emergencyMenu(Model model) {
        model.addAttribute("shutdownUrl", shutdownUrl);
        return "main/emergency/emergency-menu";
    }

}
