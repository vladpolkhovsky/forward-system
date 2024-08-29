package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.services.core.AuthorTableViewService;
import by.forward.forward_system.core.services.core.DisciplineService;
import by.forward.forward_system.core.services.core.UserTableViewService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class TableViewController {

    private final UserUiService userUiService;

    private final AuthorTableViewService authorTableViewService;

    private final UserTableViewService userTableViewService;

    private final DisciplineService disciplineService;

    @GetMapping("/author-view-{discipline}")
    public String viewAuthor(@PathVariable String discipline,
                             @RequestParam(value = "col", defaultValue = "username") String col,
                             @RequestParam(value = "order", defaultValue = "asc") String order,
                             Model model) {
        userUiService.checkAccessManager();

        if (discipline.equals("all")) {
            model.addAttribute("discipline", "Все дисциплины");
        } else {
            model.addAttribute("discipline", disciplineService.getById(Long.parseLong(discipline)).getName());
        }

        model.addAttribute("url", "/author-view-" + discipline);
        model.addAttribute("menuName", "Просмотр авторов");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("users", authorTableViewService.get(discipline, col, order));
        model.addAttribute("disciplines", disciplineService.allDisciplines());

        return "main/table-view-author";
    }

    @PostMapping("/author-view")
    public RedirectView viewAuthor(@RequestBody MultiValueMap<String, String> body) {
        userUiService.checkAccessManager();

        String discipline = body.getFirst("discipline");

        return new RedirectView("/author-view-" + discipline);
    }

    @GetMapping("/user-view")
    public String viewUser(@RequestParam(value = "col", defaultValue = "username") String col,
                           @RequestParam(value = "order", defaultValue = "asc") String order,
                           Model model) {
        userUiService.checkAccessManager();

        model.addAttribute("menuName", "Просмотр пользователей");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("users", userTableViewService.get(col, order));

        return "main/table-view-user";
    }
}
