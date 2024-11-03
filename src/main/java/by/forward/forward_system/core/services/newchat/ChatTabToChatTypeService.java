package by.forward.forward_system.core.services.newchat;

import by.forward.forward_system.core.enums.ChatType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatTabToChatTypeService {

    public String getChatTypeByTab(String tabName) {
        if (tabName.equals("new-orders")) {

            return ChatType.REQUEST_ORDER_CHAT.getName();
        } else if (tabName.equals("admin")) {

            return ChatType.ADMIN_TALK_CHAT.getName();
        } else if (tabName.equals("orders")) {

            return ChatType.ORDER_CHAT.getName();
        } else if (tabName.equals("special")) {

            return ChatType.SPECIAL_CHAT.getName();
        }
        return "";
    };
}