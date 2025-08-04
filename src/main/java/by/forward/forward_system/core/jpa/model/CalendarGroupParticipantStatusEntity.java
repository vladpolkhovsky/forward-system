package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "calendar_group_participant_status", schema = "forward_system")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CalendarGroupParticipantStatusEntity {

    @EmbeddedId
    private CalendarGroupParticipantStatusEntityId id;

}