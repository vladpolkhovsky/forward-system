package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.ui.MenuUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class
MainMenuController {

    private final UserUiService userUiService;

    private final MenuUiService menuUiService;

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/main");
    }

    @GetMapping("/main")
    public String mainMenu(Model model) {

        model.addAttribute("menu", menuUiService.getMenuEntries());
        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/main";
    }

}
