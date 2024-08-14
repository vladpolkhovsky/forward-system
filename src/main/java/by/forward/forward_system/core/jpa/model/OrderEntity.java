package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders", schema = "forward_system")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "discipline", nullable = false, length = 2048)
    private String discipline;

    @Column(name = "subject", nullable = false, length = 2048)
    private String subject;

    @Column(name = "originality", nullable = false)
    private Integer originality;

    @Column(name = "verification_system")
    private String verificationSystem;

    @Column(name = "intermediate_deadline", nullable = false)
    private LocalDateTime intermediateDeadline;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "other", length = 2048)
    private String other;

    @Column(name = "taking_cost", nullable = false)
    private Integer takingCost;

    @Column(name = "author_cost", nullable = false)
    private Integer authorCost;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order")
    private List<OrderAttachment> orderAttachment = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    private List<OrderParticipantEntity> orderParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    private List<ChatEntity> chats = new ArrayList<>();
}