package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class HelpMenuComponent implements MenuComponent {

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.AUTHOR);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = List.of(
            new MenuEntry.MenuItem("Скачать памятку", "/load-server-file/help", false, null)
        );

        return new MenuEntry("Помощь", list, 8);
    }
}
