package by.forward.forward_system.core.dto;

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
public class TagDto {
    private Long id;
    private String name;
    @Builder.Default
    private TagType type = TagType.SIMPLE;
    @Builder.Default
    private Boolean isPrimary = false;
    @Builder.Default
    private Boolean isVisible = true;
    @Builder.Default
    private Boolean isPersonal = false;
    private Long userId;
    @Builder.Default
    private TagCssColor cssColorName = TagCssColor.secondary;
}
