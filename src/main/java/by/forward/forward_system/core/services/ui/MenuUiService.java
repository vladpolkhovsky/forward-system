package by.forward.forward_system.core.services.ui;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import by.forward.forward_system.core.services.ui.menu.MenuComponent;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class MenuUiService {

    private final List<MenuComponent> menuComponents;

    public List<MenuEntry> getMenuEntries() {
        UserDetails currentUserDetails = AuthUtils.getCurrentUserDetails();

        List<MenuEntry> menuEntries = new ArrayList<>();

        for (MenuComponent menuComponent : menuComponents) {
            if (menuComponent.checkAccess(currentUserDetails.getAuthorities())) {
                menuEntries.add(menuComponent.getMenuEntry());
            }
        }

        menuEntries.sort(Comparator.comparing(MenuEntry::entryName));

        return menuEntries;
    }

}
