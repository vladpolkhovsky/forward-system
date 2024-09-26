package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "attachments", schema = "forward_system")
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "filename", nullable = false, length = 2048)
    private String filename;

    @Column(name = "filepath", nullable = false, length = 2048)
    private String filepath;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;
}