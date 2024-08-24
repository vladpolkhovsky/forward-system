package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.rest.DisciplineDto;
import by.forward.forward_system.core.services.core.DisciplineService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class SettingsController {

    private final UserUiService userUiService;

    private final DisciplineService disciplineService;

    @GetMapping(value = "/settings-discipline")
    public String disciplines(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("menuName", "Просмотр дисциплин.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("newDiscipline", new DisciplineDto());

        return "main/settings-discipline";
    }

    @PostMapping(value = "/settings-new-discipline")
    public RedirectView newDisciplines(@ModelAttribute("newDiscipline") DisciplineDto disciplineDto, Model model) {
        userUiService.checkAccessAdmin();

        disciplineService.addDiscipline(disciplineDto.getName());

        model.addAttribute("menuName", "Просмотр дисциплин.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("newDiscipline", new DisciplineDto());

        return new RedirectView("/settings-discipline");
    }

    @PostMapping(value = "/settings-update-discipline")
    public RedirectView editDisciplines(@ModelAttribute("newDiscipline") DisciplineDto disciplineDto, Model model) {
        userUiService.checkAccessAdmin();

        disciplineService.editDiscipline(disciplineDto.getId(), disciplineDto.getName());

        model.addAttribute("menuName", "Просмотр дисциплин.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("newDiscipline", new DisciplineDto());

        return new RedirectView("/settings-discipline");
    }
}
