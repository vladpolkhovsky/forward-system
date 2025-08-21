package by.forward.forward_system.core.dto.messenger.v3.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3MessageSearchCriteria {
    private Long chatId;
    @Builder.Default
    private LocalDateTime afterTime = LocalDateTime.now();
}
