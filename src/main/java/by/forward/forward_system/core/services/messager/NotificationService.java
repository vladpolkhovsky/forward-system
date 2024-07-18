package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.jpa.model.OrderEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {

    private final ChatService chatService;

    private final MessageService messageService;

    public void newOrderRequest(UserEntity userEntity, OrderEntity orderEntity) {

    }

}
