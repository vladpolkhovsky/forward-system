package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.ui.MenuUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class WebPushController {

    private final MenuUiService menuUiService;

    private final UserUiService userUiService;

    @GetMapping("/push-subscription")
    public String subscription(Model model) {

        model.addAttribute("menu", menuUiService.getMenuEntries());
        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/push-subscription";
    }
}
