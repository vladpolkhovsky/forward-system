package by.forward.forward_system.core.jpa.repository.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ForwardOrderChatProjection {
    private Long id;
    private Long chatId;
    private Long adminChatId;
}
