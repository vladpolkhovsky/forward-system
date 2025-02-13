package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_saved_to_user", schema = "forward_system")
public class SavedChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
