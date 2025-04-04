package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.AuthorUiDto;
import by.forward.forward_system.core.services.core.DisciplineService;
import by.forward.forward_system.core.services.core.UserDeletionService;
import by.forward.forward_system.core.services.ui.AuthorUiService;
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
public class CreateAuthorController {

    private final UserUiService userUiService;

    private final AuthorUiService authorUiService;

    private final DisciplineService disciplineService;
    private final UserDeletionService userDeletionService;

    @GetMapping("/create-author")
    public String createAuthor(Model model) {
        userUiService.checkAccessHR();

        model.addAttribute("menuName", "Создать автора");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("user", new AuthorUiDto());
        model.addAttribute("actionUrl", "/create-author");
        model.addAttribute("passwordRequired", true);

        return "main/create-author";
    }

    @GetMapping("/update-author")
    public String updateAuthor(Model model) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Выберите автора для изменения");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("userList", authorUiService.getAllAuthorsFast());

        return "main/update-author-selector";
    }

    @GetMapping("/update-author/{id}")
    public String updateAuthor(Model model, @PathVariable Long id) {
        userUiService.checkAccessOwner();

        model.addAttribute("menuName", "Изменение автора");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("user", authorUiService.getAuthor(id));
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("passwordRequired", false);

        model.addAttribute("actionUrl", "/update-author/" + id);

        return "main/create-author";
    }

    @PostMapping(value = "create-author", consumes = MediaType.ALL_VALUE)
    public RedirectView createAuthor(@ModelAttribute AuthorUiDto user) {
        userUiService.checkAccessHR();

        authorUiService.createAuthor(user);

        return new RedirectView("/create-author?userCreated");
    }

    @PostMapping(value = "update-author/{id}", consumes = MediaType.ALL_VALUE)
    public RedirectView updateAuthor(@PathVariable Long id, @ModelAttribute AuthorUiDto user) {
        userUiService.checkAccessOwner();

        authorUiService.updateAuthor(id, user);

        return new RedirectView("/update-author?userUpdated");
    }

    @PostMapping(value = "/update-author/delete/{id}", consumes = MediaType.ALL_VALUE)
    public RedirectView deleteUser(@PathVariable Long id) {
        userUiService.checkAccessOwner();

        userDeletionService.deleteUser(id);

        return new RedirectView("/update-author?userUpdated");
    }

}
