package by.forward.forward_system.core.services.ui.menu.impl;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.services.core.UpdateRequestOrderService;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class AdminMenuComponent implements MenuComponent {

    private final UpdateRequestOrderService updateRequestOrderService;

    @Override
    public boolean checkAccess(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(Authority.OWNER) || authorities.contains(Authority.ADMIN);
    }

    @Override
    public MenuEntry getMenuEntry() {
        List<MenuEntry.MenuItem> list = Arrays.asList(
            new MenuEntry.MenuItem("Подтвердить заказы на рассмотрении", "/order-review", true, updateRequestOrderService.countUpdateRequests()),
            new MenuEntry.MenuItem("Просмотр статистики предложения заказов", "/req-stat-redirect", false, null),
            new MenuEntry.MenuItem("Лог предложения заказов", "/order-send-log", false, null),
            new MenuEntry.MenuItem("Создать план", "/create-plan", false, null),
            new MenuEntry.MenuItem("Просмотр планов", "/view-plan", false, null),
            new MenuEntry.MenuItem("Конструктор расписания экспертов", "/calendar-constructor", false, null),
            new MenuEntry.MenuItem("Добавление / удаление участников чата", "/add-manager-to-chat", false, null)
        );
        return new MenuEntry("Админ", list, 1);
    }
}