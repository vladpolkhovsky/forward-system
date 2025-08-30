package by.forward.forward_system.core.dto.messenger.v3.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3OptionDto {
    private Long id;
    private String name;
    private String url;
    private Boolean isResolved;
}
