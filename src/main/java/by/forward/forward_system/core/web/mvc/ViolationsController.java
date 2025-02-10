package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.model.AiViolationEntity;
import by.forward.forward_system.core.jpa.repository.AiViolationRepository;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@AllArgsConstructor
public class ViolationsController {

    private final static int pageSize = 50;
    private final UserUiService userUiService;
    private final AiViolationRepository aiViolationRepository;

    @GetMapping("/violations")
    public String getViolations(Model model, @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        int count = (int) aiViolationRepository.count();
        int pageCount = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;

        List<Integer> pages = IntStream.range(1, pageCount + 1).boxed().toList();

        page = Math.max(1, Math.min(page, pageCount));
        List<AiViolationEntity> entities = aiViolationRepository.getPage(pageSize, pageSize * (page - 1));

        List<Violation> content = entities.stream()
            .map(t -> new Violation(t.getUser().getUsername(), t.getCreatedAt(), t.getAiIntegration().getId()))
            .toList();

        model.addAttribute("violations", content);
        model.addAttribute("pages", pages);
        model.addAttribute("page", page);
        model.addAttribute("menuName", "Выявленные нарушения");
        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/violations-view";
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Violation {
        private String username;
        private LocalDateTime createdAt;
        private Long aiLogId;
    }
}
