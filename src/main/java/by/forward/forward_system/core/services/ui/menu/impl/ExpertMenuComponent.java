package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import by.forward.forward_system.core.services.core.ReviewService;
import by.forward.forward_system.core.services.core.UserService;
import by.forward.forward_system.core.services.ui.UserUiService;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class ExpertMenuComponent implements MenuComponent {

    private final ReviewService reviewService;

    private final UserUiService userUiService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.AUTHOR);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<ReviewProjectionDto> notReviewedByUser = reviewService.getNotReviewedByUser(userUiService.getCurrentUserId());

        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Просмотреть запросы на проверку", "/expert-review-requests", true, notReviewedByUser.size())
        );

        return new MenuEntry("Меню Эксперта", list);
    }
}
