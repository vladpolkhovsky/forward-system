package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class ManagerMenuComponent implements MenuComponent {

    private final OrderService orderService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.ADMIN) || authorities.contains(Authority.MANAGER);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Создать заказ", "/create-order", false, null),
            new MenuEntry.MenuItem("Изменить заказ", "/update-order", false, null),
            new MenuEntry.MenuItem("Просмотреть заказы", "/view-order", true, orderService.countNotClosed())
        );
        return new MenuEntry("Меню заказов", list);
    }

}
