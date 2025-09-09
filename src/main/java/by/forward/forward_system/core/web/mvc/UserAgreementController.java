package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserAgreementController {

    private final UserRepository userRepository;

    @GetMapping("/user-agreement")
    public String getUserAgreementPage(Model model) {
        Optional<UserEntity> byId = userRepository.findById(AuthUtils.getCurrentUserId());
        if (byId.isEmpty()) {
            return "redirect:/main";
        }

        UserEntity userEntity = byId.get();
        if (userEntity.getNewAgreementSigned()) {
            return "redirect:/main";
        }

        return "user-agreement/view";
    }

    @PostMapping("/user-agreement")
    @Transactional
    public String handleSignUserAgreement(@RequestParam(value = "is-agree", defaultValue = "false") Boolean isUserAgree) {
        Optional<UserEntity> byId = userRepository.findById(AuthUtils.getCurrentUserId());

        if (byId.isEmpty()) {
            return "redirect:/main";
        }

        if (!isUserAgree) {
            return "redirect:/user-agreement?notSigned";
        }

        byId.ifPresent(t -> t.setNewAgreementSigned(true));

        return "redirect:/main";
    }
}
