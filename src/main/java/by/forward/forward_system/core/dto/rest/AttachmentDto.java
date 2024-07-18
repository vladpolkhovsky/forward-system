package by.forward.forward_system.core.dto.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentDto {
    private String fileName;
    private String base64content;
}
