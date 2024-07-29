package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reviews", schema = "forward_system")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private AttachmentEntity attachment;

    @Column(name = "review_message", length = 16384)
    private String reviewMessage;

    @Column(name = "review_verdict", length = 16384)
    private String reviewVerdict;

    @Column(name = "is_reviewed", nullable = false)
    private Boolean isReviewed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewedBy;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}