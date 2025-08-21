package by.forward.forward_system.core.kafka.events;

import by.forward.forward_system.core.dto.messenger.v3.ChatCreationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatEvent {
    private ChatCreationDto dto;
    private String initialMessage;
    private Boolean addAdminAutomatically;
}
