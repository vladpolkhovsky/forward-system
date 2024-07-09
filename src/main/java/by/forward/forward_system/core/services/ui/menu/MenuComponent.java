package by.forward.forward_system.core.services.ui.menu;

import by.forward.forward_system.core.dto.ui.MenuEntry;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface MenuComponent {

    boolean checkAccess(Collection<? extends GrantedAuthority> authorities);

    MenuEntry getMenuEntry();

}
