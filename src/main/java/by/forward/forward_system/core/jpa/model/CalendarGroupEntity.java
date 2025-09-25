package by.forward.forward_system.core.jpa.model;

import by.forward.forward_system.core.dto.rest.calendar.CalendarGroupDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "calendar_group", schema = "forward_system")
public class CalendarGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @ColumnDefault("now()")
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "forward_system", name = "calendar_group_participant",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> participants = new LinkedHashSet<>();

    public static CalendarGroupEntity of(Long id) {
        if (id == null) {
            return null;
        }
        CalendarGroupEntity calendarGroupEntity = new CalendarGroupEntity();
        calendarGroupEntity.setId(id);
        return calendarGroupEntity;
    }
}
