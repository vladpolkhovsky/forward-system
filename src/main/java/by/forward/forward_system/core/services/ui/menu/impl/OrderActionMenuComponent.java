package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.ReviewService;
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
public class OrderActionMenuComponent implements MenuComponent {

    private final ReviewService reviewService;

    private final OrderService orderService;

    private final OrderUiService orderUiService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.ADMIN) || authorities.contains(Authority.MANAGER);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Создать заказ", "/create-order", false, null),
            new MenuEntry.MenuItem("Изменить заказ", "/update-order", false, null),
            new MenuEntry.MenuItem("Просмотреть все заказы", "/view-order", true, orderService.countNotClosed()),
            new MenuEntry.MenuItem("Мои заказы", "/view-my-order", true, orderUiService.countMyOrders()),
            new MenuEntry.MenuItem("Отправить заказ в работу", "/order-to-in-progress", false, null),
            new MenuEntry.MenuItem("Отправить заказ на проверку", "/review-order", false, null),
            new MenuEntry.MenuItem("Изменить запрос на проверку", "/edit-review-order", false, null),
            new MenuEntry.MenuItem("Посмотреть ответы экспертов", "/review/manager-list-view", true, reviewService.getNotAcceptedCount()),
            new MenuEntry.MenuItem("Изменить статус заказа", "/change-order-status", true, orderService.countFinalStatusOrders()),
            new MenuEntry.MenuItem("Добавить автора в заказ", "/add-author-to-order", false, null),
            new MenuEntry.MenuItem("Удалить автора из заказа", "/del-author-from-order", false, null),
            new MenuEntry.MenuItem("Изменение долей в заказе", "/change-fee-in-order", false, null)
        );
        return new MenuEntry("Заказы", list, 6);
    }

}
