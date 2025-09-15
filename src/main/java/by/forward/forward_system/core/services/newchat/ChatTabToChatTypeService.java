package by.forward.forward_system.core.services.newchat;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ChatTabToChatTypeService {

    public List<String> getChatTypeByTab(String tabName) {
        if (tabName == null) {
            return null;
        }

        if (tabName.equals("saved")) {
            return List.of("use-saved");
        } else if (tabName.equals("new-orders")) {
            return List.of(ChatType.REQUEST_ORDER_CHAT.getName());
        } else if (tabName.equals("admin")) {
            return List.of(ChatType.ADMIN_TALK_CHAT.getName(), ChatType.FORWARD_ORDER_ADMIN_CHAT.getName());
        } else if (tabName.equals("orders")) {
            return List.of(ChatType.ORDER_CHAT.getName(), ChatType.FORWARD_ORDER_CHAT.getName());
        } else if (tabName.equals("special")) {
            return List.of(ChatType.SPECIAL_CHAT.getName());
        }

        return null;
    }

    ;

    public String getChatTabByType(String chatType) {
        if (chatType == null) {
            return null;
        }
        if (chatType.equals(ChatType.REQUEST_ORDER_CHAT.getName())) {

            return "new-orders";
        } else if (chatType.equals(ChatType.ADMIN_TALK_CHAT.getName())) {

            return "admin";
        } else if (chatType.equals(ChatType.ORDER_CHAT.getName())) {

            return "orders";
        } else if (chatType.equals(ChatType.SPECIAL_CHAT.getName())) {

            return "special";
        }
        return null;
    }
}
