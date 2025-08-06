package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class AuthorMenuComponent implements MenuComponent {

    private final OrderUiService orderUiService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.AUTHOR);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Мои заказы", "/view-my-order-author", true, orderUiService.countMyOrders()),
            new MenuEntry.MenuItem("Мои выплаты", "/author-view-payment", false, null),
            new MenuEntry.MenuItem("Мои статусы выплат по заказам", "/accountant/order-payment-status", false, null)
        );

        return new MenuEntry("Автор", list, 2);
    }
}
