package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.PaymentStatus;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.PaymentFileRepository;
import by.forward.forward_system.core.jpa.repository.PaymentRepository;
import by.forward.forward_system.core.jpa.repository.PaymentStatusRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.PaymentDto;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentStatusRepository paymentStatusRepository;

    private final PaymentFileRepository paymentFileRepository;

    private final AttachmentService attachmentService;

    private final BotNotificationService botNotificationService;

    private final UserRepository userRepository;

    private final MessageService messageService;

    @Transactional
    @SneakyThrows
    public void createPayment(Long userId, Long createdByUserId, String accMessage, String paymentNumber, MultipartFile accFile) {
        PaymentStatusEntity status = paymentStatusRepository.findById(PaymentStatus.WAITING_SIGNED_FILE.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));
        AttachmentEntity attachmentEntity = attachmentService.saveAttachment(accFile.getOriginalFilename(), accFile.getBytes());

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        UserEntity createdByUser = userRepository.findById(createdByUserId).orElseThrow(() -> new RuntimeException("User Not Found"));

        LocalDateTime now = LocalDateTime.now();

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentNumber(paymentNumber);
        paymentEntity.setAccMessage(accMessage);
        paymentEntity.setAccAttachment(attachmentEntity);

        paymentEntity.setUser(user);
        paymentEntity.setCreatedByUser(createdByUser);

        paymentEntity.setStatus(status);

        paymentEntity.setCreatedAt(now);
        paymentEntity.setUpdatedAt(now);

        PaymentEntity save = paymentRepository.save(paymentEntity);

        PaymentFileEntity paymentFileEntity = new PaymentFileEntity();
        paymentFileEntity.setPayment(save);
        paymentFileEntity.setAttachment(attachmentEntity);

        paymentFileRepository.save(paymentFileEntity);

        botNotificationService.sendBotNotification(userId, """
            Здравствуйте, %s
            Для вас сформирована выплата: %s
            Проверьте свои выплаты на сайте и следуйте указаниям.
            """.formatted(user.getUsername(), paymentNumber));

        sendMessageToAdminChat(userId, """
            Для автора %s создана выплата: %s
            Проверьте свои выплаты на сайте и следуйте указаниям.
            """.formatted(user.getUsername(), paymentNumber));
    }

    public long countPages() {
        long count = paymentRepository.count();
        return (count / Constants.PAYMENT_PAGE_SIZE) + (count % Constants.PAYMENT_PAGE_SIZE == 0 ? 0 : 1);
    }

    public long countPages(long userId) {
        long count = paymentRepository.countByUser(userId);
        return (count / Constants.PAYMENT_PAGE_SIZE) + (count % Constants.PAYMENT_PAGE_SIZE == 0 ? 0 : 1);
    }

    public List<PaymentDto> getAllPayments(long page) {
        return paymentRepository.getAllPaymentPage((page - 1) * Constants.PAYMENT_PAGE_SIZE, Constants.PAYMENT_PAGE_SIZE);
    }

    public List<PaymentDto> getUserPayments(long userId, long page) {
        return paymentRepository.getUserPaymentPage(userId, (page - 1) * Constants.PAYMENT_PAGE_SIZE, Constants.PAYMENT_PAGE_SIZE);
    }

    public boolean isPaymentUser(Long paymentId, Long currentUserId) {
        return paymentRepository.paymentExists(paymentId, currentUserId);
    }

    public boolean isPaymentStatus(Long paymentId, PaymentStatus paymentStatus) {
        return paymentRepository.isPaymentWithStatus(paymentId, paymentStatus.getName());
    }

    public PaymentEntity getPayment(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment Not Found"));
    }

    @SneakyThrows
    @Transactional
    public void saveSignedFile(Long paymentId, String userMessage, MultipartFile signedFile) {
        PaymentEntity payment = getPayment(paymentId);

        if (payment.getStatus().getStatus() != PaymentStatus.WAITING_SIGNED_FILE) {
            throw new IllegalStateException("Неправильный статус выплаты id = %d. Статус: %s. Ожидалось: %s.".formatted(
                paymentId,
                payment.getStatus().getStatus(),
                PaymentStatus.WAITING_SIGNED_FILE)
            );
        }

        PaymentStatusEntity newStatus = paymentStatusRepository.findById(PaymentStatus.WAITING_PAYMENT.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));

        AttachmentEntity attachmentEntity = attachmentService.saveAttachment(signedFile.getOriginalFilename(), signedFile.getBytes());

        payment.setUserMessage(userMessage);
        payment.setStatus(newStatus);
        payment.setUserAttachment(attachmentEntity);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        PaymentFileEntity paymentFileEntity = new PaymentFileEntity();
        paymentFileEntity.setPayment(payment);
        paymentFileEntity.setAttachment(attachmentEntity);

        paymentFileRepository.save(paymentFileEntity);

        botNotificationService.sendBotNotification(payment.getCreatedByUser().getId(), """
            Изменён статус выплаты: %s.
            Автор прислал подписанный файл.
            """.formatted(payment.getPaymentNumber()));

        sendMessageToAdminChat(payment.getUser().getId(), """
            Изменён статус выплаты: %s.
            Автор прислал подписанный файл.
            """.formatted(payment.getPaymentNumber()));
    }

    @Transactional
    public void confirmPayment(Long paymentId) {
        PaymentEntity payment = getPayment(paymentId);

        if (payment.getStatus().getStatus() != PaymentStatus.WAITING_PAYMENT) {
            throw new IllegalStateException("Неправильный статус выплаты id = %d. Статус: %s. Ожидалось: %s.".formatted(
                paymentId,
                payment.getStatus().getStatus(),
                PaymentStatus.WAITING_PAYMENT)
            );
        }

        PaymentStatusEntity newStatus = paymentStatusRepository.findById(PaymentStatus.WAITING_CHECK.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        botNotificationService.sendBotNotification(payment.getUser().getId(), """
            Здравствуйте, %s.
            Выплата %s успешно прозведена.
            Прверьте карточку выплаты и вышлите чек.
            """.formatted(payment.getUser().getUsername(), payment.getPaymentNumber()));

        sendMessageToAdminChat(payment.getUser().getId(), """
            Выплата %s для автора %s успешно прозведена.
            Прверьте карточку выплаты и вышлите чек.
            """.formatted(payment.getPaymentNumber(), payment.getUser().getUsername()));
    }

    @Transactional
    @SneakyThrows
    public void saveCheckFile(Long paymentId, MultipartFile checkFile) {
        PaymentEntity payment = getPayment(paymentId);

        if (payment.getStatus().getStatus() != PaymentStatus.WAITING_CHECK) {
            throw new IllegalStateException("Неправильный статус выплаты id = %d. Статус: %s. Ожидалось: %s.".formatted(
                paymentId,
                payment.getStatus().getStatus(),
                PaymentStatus.WAITING_CHECK)
            );
        }

        AttachmentEntity attachmentEntity = attachmentService.saveAttachment(checkFile.getOriginalFilename(), checkFile.getBytes());

        PaymentStatusEntity newStatus = paymentStatusRepository.findById(PaymentStatus.VERIFICATION.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));

        payment.setStatus(newStatus);
        payment.setUserCheckAttachment(attachmentEntity);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        PaymentFileEntity paymentFileEntity = new PaymentFileEntity();
        paymentFileEntity.setPayment(payment);
        paymentFileEntity.setAttachment(attachmentEntity);

        paymentFileRepository.save(paymentFileEntity);

        botNotificationService.sendBotNotification(payment.getCreatedByUser().getId(), """
            Изменён статус выплаты: %s.
            Автор прислал чек.
            """.formatted(payment.getPaymentNumber()));

        sendMessageToAdminChat(payment.getUser().getId(), """
            Изменён статус выплаты: %s.
            Автор прислал чек.
            """.formatted(payment.getPaymentNumber()));
    }

    public void closePayment(Long paymentId) {
        PaymentEntity payment = getPayment(paymentId);

        if (payment.getStatus().getStatus() != PaymentStatus.VERIFICATION) {
            throw new IllegalStateException("Неправильный статус выплаты id = %d. Статус: %s. Ожидалось: %s.".formatted(
                paymentId,
                payment.getStatus().getStatus(),
                PaymentStatus.VERIFICATION)
            );
        }

        PaymentStatusEntity newStatus = paymentStatusRepository.findById(PaymentStatus.CLOSED.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        sendMessageToAdminChat(payment.getUser().getId(), """
            Выплата %s успешно завершена.
            """.formatted(payment.getPaymentNumber()));
    }

    public void annulPayment(Long paymentId, String reason) {
        PaymentEntity payment = getPayment(paymentId);
        PaymentStatusEntity newStatus = paymentStatusRepository.findById(PaymentStatus.ANNULLED.getName()).orElseThrow(() -> new RuntimeException("Payment Status Not Found"));

        payment.setStatus(newStatus);
        payment.setAnnulledReason(reason);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        botNotificationService.sendBotNotification(payment.getUser().getId(), """
            Выплата %s аннулирована.
            Проверьте карточку выплаты, чтобы узнать причину.
            """.formatted(payment.getPaymentNumber()));

        sendMessageToAdminChat(payment.getUser().getId(), """
            Выплата %s аннулирована.
            Причина аннулирования в карточе выплаты.
            """.formatted(payment.getPaymentNumber()));
    }

    private void sendMessageToAdminChat(Long userId, String message) {
        messageService.sendMessageToAdminChat(userId, message);
    }
}
