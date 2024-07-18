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
public class ManagerMenuComponent implements MenuComponent {

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.ADMIN) || authorities.contains(Authority.MANAGER);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Создать заказ", "/create-order"),
            new MenuEntry.MenuItem("Изменить заказ", "/update-order"),
            new MenuEntry.MenuItem("Прсмотреть заказы", "/view-order"),
            new MenuEntry.MenuItem("Отправить заказ в работу", "/order-to-in-progress")
        );

        return new MenuEntry("Меню заказов", list);
    }

}
