package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class CreateUserController {

    private final UserUiService userUiService;

    @GetMapping("/create-user")
    public String createUser(Model model) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Создать пользователя");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("user", new UserUiDto());
        model.addAttribute("actionUrl", "/create-user");
        model.addAttribute("passwordRequired", true);

        return "main/create-user";
    }

    @GetMapping("/update-user")
    public String updateUser(Model model) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Выберите пользователя для изменения");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("userList", userUiService.getAllUsers());

        return "main/update-user-selector";
    }

    @GetMapping("/update-user/{id}")
    public String updateUser(Model model, @PathVariable Long id) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Изменение пользователя");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("user", userUiService.getUser(id));
        model.addAttribute("passwordRequired", false);

        model.addAttribute("actionUrl", "/update-user/" + id);

        return "main/create-user";
    }

    @PostMapping(value = "create-user", consumes = MediaType.ALL_VALUE)
    public RedirectView createUser(@ModelAttribute UserUiDto user) {
        userUiService.checkAccessOwner();

        userUiService.createUser(user);

        return new RedirectView("/create-user?userCreated");
    }

    @PostMapping(value = "update-user/{id}", consumes = MediaType.ALL_VALUE)
    public RedirectView updateUser(@PathVariable Long id, @ModelAttribute UserUiDto user) {
        userUiService.checkAccessOwner();

        userUiService.updateUser(id, user);

        return new RedirectView("/update-user?userUpdated");
    }
}
