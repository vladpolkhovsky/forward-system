package by.forward.forward_system.core.dto.rest;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalDateDto {
    private String text;
    @With
    private String time;
}
