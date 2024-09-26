package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders", schema = "forward_system")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 2048)
    private String name;

    @Column(name = "tech_number", nullable = false)
    private String techNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_status", nullable = false)
    private OrderStatusEntity orderStatus;

    @Column(name = "work_type", nullable = false)
    private String workType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "discipline_id", nullable = false)
    private DisciplineEntity discipline;

    @Column(name = "subject", nullable = false, length = 2048)
    private String subject;

    @Column(name = "originality", nullable = false)
    private Integer originality;

    @Column(name = "verification_system")
    private String verificationSystem;

    @Column(name = "intermediate_deadline")
    private LocalDateTime intermediateDeadline;

    @Column(name = "additional_dates")
    private String additionalDates;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "other")
    private String other;

    @Column(name = "taking_cost", nullable = false)
    private Integer takingCost;

    @Column(name = "author_cost", nullable = false)
    private Integer authorCost;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JoinColumn(name = "created_by")
    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity createdBy;

    @OneToMany(mappedBy = "order")
    private List<OrderAttachmentEntity> orderAttachment = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    private List<OrderParticipantEntity> orderParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    private List<ChatEntity> chats = new ArrayList<>();

}