package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.OrderUiDto;
import by.forward.forward_system.core.jpa.repository.projections.BanProjectionDto;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;

@Controller
@AllArgsConstructor
public class BanController {

    private final UserUiService userUiService;

    private final BanService banService;

    @GetMapping(value = "/auto-ban-user")
    public String autoBanUserSelector(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Недавно заблокированные пользователи");
        model.addAttribute("banned", banService.getAllNewBanned());

        return "main/auto-ban-user-selector";
    }

    @GetMapping(value = "/ban-user")
    public String banUserSelector(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заблокированные пользователи");
        model.addAttribute("banned", banService.getAllBanned());

        return "main/ban-user-selector";
    }

    @GetMapping(value = "/auto-ban-user/{banId}")
    public String autoBanUserSelector(Model model, @PathVariable Long banId) {
        userUiService.checkAccessAdmin();

        BanProjectionDto banned = banService.getBanned(banId);

        model.addAttribute("menuName", "Недавно заблокированный пользователь");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ban", banned);

        return "main/auto-ban-user";
    }

    @GetMapping(value = "/auto-ban-user-verdict/{banId}")
    private RedirectView autoBanUserVerdict(@PathVariable Long banId, @RequestParam("ban") Boolean banVerdict) {
        userUiService.checkAccessAdmin();

        if (banVerdict) {
            banService.setPermanentBan(banId);
        } else {
            BanProjectionDto banned = banService.getBanned(banId);
            banService.unban(banned.getUser().getId());
        }

        return new RedirectView("/main");
    }

    @GetMapping(value = "/manual-ban-user")
    private String manualBanUser(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("users", banService.getAllUsersForBan());
        model.addAttribute("menuName", "Блокировка пользователя");
        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/manual-ban-user";
    }

    @PostMapping(value = "/manual-ban-user")
    private RedirectView manualBanUser(@RequestBody MultiValueMap<String, String> body) {
        userUiService.checkAccessAdmin();

        Long userId = Long.parseLong(body.getFirst("userId"));
        String reason = body.getFirst("reason");
        banService.ban(userId, reason, true, Collections.emptyList());

        return new RedirectView("/main");
    }

    @GetMapping(value = "/ban")
    private String ban(Model model) {
        BanProjectionDto bannedByUserId = banService.getBannedByUserId(userUiService.getCurrentUserId());
        model.addAttribute("ban", bannedByUserId);
        return "main/ban-info";
    }
}
