package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.model.AiLogEntity;
import by.forward.forward_system.core.jpa.repository.AiLogRepository;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@AllArgsConstructor
public class AiController {

    private final UserUiService userUiService;
    private final AiLogRepository aiLogRepository;

    @GetMapping("/ai-log/{fileId}")
    public String log(@PathVariable Long fileId, Model model) {
        AiLogEntity byId = aiLogRepository.findById(fileId).orElseThrow(() -> new RuntimeException("Not found"));

        model.addAttribute("menuName", "Просмотр лога интеграции");
        model.addAttribute("log", byId);
        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/ai-log-info";
    }
}
