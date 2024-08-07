package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class HrMenuComponent implements MenuComponent {

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.HR);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Создать атора", "/create-author", false, null),
            new MenuEntry.MenuItem("Изменить атора", "/update-author", false, null)
        );

        return new MenuEntry("Меню HR", list);
    }
}
