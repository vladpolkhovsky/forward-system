package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.services.ui.UserUiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserInfoController {

    private final UserUiService userUiService;

    public UserInfoController(UserUiService userUiService) {
        this.userUiService = userUiService;
    }

    @GetMapping(value = "/info")
    public String info(Model model) {
        UserUiDto user = userUiService.getUser(userUiService.getCurrentUserId());
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("user", user);
        return "main/info";
    }

}
