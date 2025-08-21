package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.enums.TagCssColor;
import by.forward.forward_system.core.enums.TagType;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "tags", schema = "forward_system")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 75)
    private TagType type;

    @ColumnDefault("false")
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @ColumnDefault("true")
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = false;

    @ColumnDefault("false")
    @Column(name = "is_personal", nullable = false)
    private Boolean isPersonal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_user_id")
    private UserEntity associatedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "css_color_name", length = 75)
    private TagCssColor cssColorName;

    @Type(value = PostgreSQLTSVectorType.class)
    @Column(name = "tsvector_tag_name", updatable = false, insertable = false, nullable = false)
    private String tsvector;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagEntity that = (TagEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}