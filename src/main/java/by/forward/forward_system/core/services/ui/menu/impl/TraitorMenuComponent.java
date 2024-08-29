package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.core.BanService;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class TraitorMenuComponent implements MenuComponent {

    private final BanService banService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.ADMIN) || authorities.contains(Authority.OWNER);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Автоматическая блокировка", "/auto-ban-user", true, banService.countNewBaned()),
            new MenuEntry.MenuItem("Заблокированные пользователи", "/ban-user", false, null),
            new MenuEntry.MenuItem("Заблокировать пользователя", "/manual-ban-user", false, null)
        );

        return new MenuEntry("Безопасность чатов", list, 6);
    }
}
