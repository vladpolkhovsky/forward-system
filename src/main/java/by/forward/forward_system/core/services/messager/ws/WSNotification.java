package by.forward.forward_system.core.services.messager.ws;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WSNotification<T> {
    private String type;
    private T value;
    public static final class NotificationTypes {
        public static final String MESSAGE = "message";
        public static final String NEW_CHAT = "new-chat";
    }
}
