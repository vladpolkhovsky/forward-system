package by.forward.forward_system.core.events.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckMessageByAiEventDto {
    private Long userId;
    private Long chatId;
    private Optional<String> message;
    private List<Long> attachmentIds;
}
