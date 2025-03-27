package by.forward.forward_system.core.events.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotifyForwardOrderCustomersEvent {
    private Long forwardOrderId;
    private Long chatMessageId;
}
