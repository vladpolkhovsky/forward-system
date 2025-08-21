package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@Entity
@Table(name = "chat_metadata", schema = "forward_system")
public class ChatMetadataEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private ChatEntity chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserEntity manager;

    @Column(name = "owner_type_permission")
    private Boolean ownerTypePermission;

    @Column(name = "author_can_submit_files")
    private Boolean authorCanSubmitFiles;

    public Long getManagerId() {
        return Optional.ofNullable(getManager())
            .map(UserEntity::getId)
            .orElse(null);
    }

    public Long getAuthorId() {
        return Optional.ofNullable(getUser())
            .map(UserEntity::getId)
            .orElse(null);
    }
}