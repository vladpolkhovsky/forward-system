package by.forward.forward_system.core.iternalnotification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationMessageDto {
    private InformationLevel informationLevel;
    private String tittle;
    private String description;
    private String fromUsername;
    private Long targetUserId;
    private Set<String> tags;
}