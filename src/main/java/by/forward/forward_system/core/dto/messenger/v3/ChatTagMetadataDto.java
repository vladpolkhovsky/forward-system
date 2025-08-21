package by.forward.forward_system.core.dto.messenger.v3;

import by.forward.forward_system.core.enums.TagCssColor;
import by.forward.forward_system.core.enums.TagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTagMetadataDto {
    @Builder.Default
    private TagType type = TagType.SIMPLE;
    @Builder.Default
    private Boolean isPrimaryTag = false;
    @Builder.Default
    private Boolean isVisible = true;
    @Builder.Default
    private Boolean isPersonalTag = false;
    private Long userId;
    @Builder.Default
    private TagCssColor cssColorName = TagCssColor.secondary;
}
