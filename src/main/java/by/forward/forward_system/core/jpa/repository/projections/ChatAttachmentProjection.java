package by.forward.forward_system.core.jpa.repository.projections;

import java.time.LocalDateTime;

public interface ChatAttachmentProjection {
    String getFirstname();

    String getLastname();

    String getUsername();

    String getAttachmentFilename();

    Long getAttachmentFileId();

    LocalDateTime getAttachmentTime();
}
