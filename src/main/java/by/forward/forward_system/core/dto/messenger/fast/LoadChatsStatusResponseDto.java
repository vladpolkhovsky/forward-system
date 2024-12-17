package by.forward.forward_system.core.dto.messenger.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadChatsStatusResponseDto {

    private List<ChatStatus> statuses;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatStatus {
        private Long id;
        private String status;
    }
}
