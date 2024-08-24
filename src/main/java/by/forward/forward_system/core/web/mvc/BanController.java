package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.repository.projections.BanProjectionDto;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

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

    @GetMapping(value = "/ban-user")
    public String banUserSelector(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заблокированные пользователи");
        model.addAttribute("banned", banService.getAllBanned());
        return "main/ban-user-selector";
    }

    @GetMapping(value = "/auto-ban-user/{banId}")
    public String autoBanUserSelector(Model model, @PathVariable Long banId) {
        BanProjectionDto banned = banService.getBanned(banId);

        model.addAttribute("menuName", "Автоматическая блокировка пользователей");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ban", banned);

        return "main/auto-ban-user";
    }

    @GetMapping(value = "/auto-ban-user-verdict/{banId}")
    private RedirectView autoBanUserVerdict(@PathVariable Long banId, @RequestParam("ban") Boolean banVerdict) {
        if (banVerdict) {
            banService.setPermanentBan(banId);
        } else {
            BanProjectionDto banned = banService.getBanned(banId);
            banService.unban(banned.getUser().getId());
        }
        return new RedirectView("/main");
    }

}
