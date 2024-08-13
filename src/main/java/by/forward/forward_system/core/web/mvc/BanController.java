package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class BanController {

    private final UserUiService userUiService;

    private final BanService banService;

    @GetMapping(value = "/auto-ban-user")
    public String autoBanUserSelector(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Автоматическая блокировка пользователей");
        model.addAttribute("banned", banService.getAllNewBanned());
        return "main/auto-ban-user-selector";
    }

    @GetMapping(value = "/auto-ban-user/{banId}")
    public String autoBanUserSelector(Model model, @PathVariable Long banId) {
        model.addAttribute("menuName", "Автоматическая блокировка пользователей");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ban", banService.getBanned(banId));
        return "main/auto-ban-user";
    }

}
