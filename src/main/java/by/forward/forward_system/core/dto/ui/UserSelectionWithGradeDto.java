package by.forward.forward_system.core.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSelectionWithGradeDto {
    private Long id;
    private String fio;
    private String username;
    private Boolean checked;
    private String bgColor;
}
