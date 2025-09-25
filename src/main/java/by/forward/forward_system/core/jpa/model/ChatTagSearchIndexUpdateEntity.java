package by.forward.forward_system.core.jpa.model;

import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "tags", schema = "forward_system")
public class ChatTagSearchIndexUpdateEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 1024, updatable = false)
    private String name;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "tsvector_tag_name", columnDefinition = "tsvector")
    private String tokens;
}
