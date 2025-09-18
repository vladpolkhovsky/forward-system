package by.forward.forward_system.core.jpa.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "message_view_stats", schema = "forward_system")
public class MessageViewStatusView {

    @Id
    @Column(name = "id")
    private Long messageId;

    @Column(name = "user_count")
    private Long viewedUserCount;

    @Type(ListArrayType.class)
    @Column(name = "usernames")
    private List<String> viewedByUsers = new ArrayList<>();
}
