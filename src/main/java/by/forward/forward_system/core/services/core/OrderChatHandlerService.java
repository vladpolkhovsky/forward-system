package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.projections.OrderChatDataProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderChatHandlerService {

    private final ChatRepository chatRepository;

    public Map<Long, OrderChatDataProjection> calcChatData(List<Long> orderIds, Long currentUserId) {
        List<OrderChatDataProjection> newMessagesForUser = chatRepository.findOrderChatNewMessagesForUser(orderIds.stream().distinct().toList(), currentUserId);
        return newMessagesForUser.stream().collect(Collectors.toMap(
            OrderChatDataProjection::getOrderId,
            t -> t,
            (u, v) -> {
                if (u.getChatId() > v.getChatId()) {
                    return u;
                }
                if (v.getChatId() > u.getChatId()) {
                    return v;
                }
                return u;
            })
        );
    }

}
