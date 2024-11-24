package by.forward.forward_system.core.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment", schema = "forward_system")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forward_system.id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserEntity createdByUser;

    @Column(name = "payment_number", nullable = false, length = 2048)
    private String paymentNumber;

    @Column(name = "acc_message", length = 65536)
    private String accMessage;

    @Column(name = "user_message", length = 65536)
    private String userMessage;

    @Column(name = "annulled_reason", length = 65536)
    private String annulledReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_attachment_id")
    private AttachmentEntity accAttachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_attachment_id")
    private AttachmentEntity userAttachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_check_attachment_id")
    private AttachmentEntity userCheckAttachment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status", nullable = false)
    private PaymentStatusEntity status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}