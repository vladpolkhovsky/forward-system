package by.forward.forward_system.core.dto.ui;

import java.util.List;

public record MenuEntry(String entryName, List<MenuItem> menuItems) {

    public record MenuItem(String name, String link) {

    }
}
