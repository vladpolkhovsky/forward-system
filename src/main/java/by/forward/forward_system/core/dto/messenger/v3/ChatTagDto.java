package by.forward.forward_system.core.dto.messenger.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTagDto {
    private Long id;
    private String name;
    private ChatTagMetadataDto metadata;
}
