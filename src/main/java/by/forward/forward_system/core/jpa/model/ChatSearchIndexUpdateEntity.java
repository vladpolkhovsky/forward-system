package by.forward.forward_system.core.jpa.model;

import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "chats", schema = "forward_system")
public class ChatSearchIndexUpdateEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "chat_name", nullable = false, length = 1024, updatable = false)
    private String chatName;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "tsvector_chat_name", columnDefinition = "tsvector")
    private String tokens;
}
