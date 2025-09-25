package by.forward.forward_system.core.dto.messenger.v3.chat;

import by.forward.forward_system.core.dto.rest.users.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V3ChatFileAttachmentDto {
    private V3AttachmentDto attachment;
    private UserDto user;
    private String createdAt;
}
