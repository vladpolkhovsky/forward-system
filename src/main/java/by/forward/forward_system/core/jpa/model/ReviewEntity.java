package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reviews", schema = "forward_system")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private AttachmentEntity attachment;

    @Column(name = "review_message")
    private String reviewMessage;

    @Column(name = "review_verdict")
    private String reviewVerdict;

    @Column(name = "review_mark")
    private String reviewMark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "review_file_id")
    private AttachmentEntity reviewAttachment;

    @Column(name = "is_reviewed", nullable = false)
    private Boolean isReviewed = false;

    @Column(name = "is_accepted", nullable = false)
    private Boolean isAccepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewedBy;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}