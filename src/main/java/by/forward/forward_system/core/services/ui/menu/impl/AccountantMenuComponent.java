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
public class AccountantMenuComponent implements MenuComponent {

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.ACCOUNTANT);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Создать выплату", "/create-payment", false, null),
            new MenuEntry.MenuItem("Просмотр выплат", "/acc-view-payment", false, null)
        );

        return new MenuEntry("Бухгалтер", list, 3);
    }
}
