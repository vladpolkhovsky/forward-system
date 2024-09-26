package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "ai_integration", schema = "forward_system")
public class AiLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "request_json", length = 65536)
    private String requestJson;

    @Column(name = "response_json", length = 65536)
    private String responseJson;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "error_text", length = 65536)
    private String errorText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
