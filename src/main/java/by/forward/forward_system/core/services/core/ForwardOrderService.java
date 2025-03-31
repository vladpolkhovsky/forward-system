package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.events.events.CheckMessageByAiEvent;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ForwardOrderProjection;
import by.forward.forward_system.core.jpa.repository.projections.NewMessageCountProjection;
import by.forward.forward_system.core.jpa.repository.projections.ReviewRequestProjection;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ForwardOrderService {

    private static DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm");

    private final ForwardOrderRepository forwardOrderRepository;

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;
    private final ForwardOrderReviewRequestRepository forwardOrderReviewRequestRepository;

    private final MessageService messageService;
    private final ChatMessageTypeRepository chatMessageTypeRepository;
    private final BotNotificationService botNotificationService;
    private final ChatMetadataRepository chatMetadataRepository;
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Map<Long, Long> newMessageCount(Long currentUserId) {
        List<NewMessageCountProjection> newMessageCountProjections = forwardOrderRepository.calcNewMessageCount(currentUserId);
        return newMessageCountProjections.stream()
            .collect(Collectors.toMap(NewMessageCountProjection::getId, NewMessageCountProjection::getCount));
    }

    public List<ForwardOrderProjection> findAllProjections(Long currentUserId, Boolean isAdmin) {
        List<ForwardOrderProjection> allProjections = forwardOrderRepository.findAllProjections();
        if (isAdmin) {
            return allProjections;
        }
        return allProjections.stream()
            .filter(t -> Objects.equals(t.getAuthorUserId(), currentUserId))
            .toList();
    }

    @SneakyThrows
    @Transactional
    public void saveReviewRequest(Long forwardOrderId, String userNote, Long fromUserId, MultipartFile file) {
        UserEntity fromUserEntity = userRepository.findById(fromUserId)
            .orElseThrow(() -> new RuntimeException("User not found id: " + fromUserId));

        ForwardOrderEntity forwardOrderEntity = forwardOrderRepository.findById(forwardOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid forward order id: " + forwardOrderId));

        AttachmentEntity attachmentEntity = attachmentService.saveAttachment(
            "Запрос на проверку ТЗ %s От %s Файл %s".formatted(
                forwardOrderEntity.getOrder().getTechNumber(),
                LocalDateTime.now().format(outputDateTimeFormat),
                StringUtils.abbreviateMiddle(file.getOriginalFilename(), "-", 10)
            ),
            file.getBytes()
        );

        ForwardOrderReviewRequestEntity requestEntity = new ForwardOrderReviewRequestEntity();
        requestEntity.setForwardOrder(forwardOrderEntity);
        requestEntity.setRequestByUser(fromUserEntity);
        requestEntity.setFile(attachmentEntity);
        requestEntity.setNote(userNote);
        requestEntity.setDone(false);
        requestEntity.setCreatedAt(LocalDateTime.now());

        requestEntity = forwardOrderReviewRequestRepository.save(requestEntity);

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        ChatMessageAttachmentEntity messageAttachmentEntity = new ChatMessageAttachmentEntity();
        messageAttachmentEntity.setAttachment(attachmentEntity);

        ChatMessageEntity message = messageService.sendMessage(
            null,
            forwardOrderEntity.getChat(),
            "Пользователь %s создал запрос на проверку файла.".formatted(fromUserEntity.getUsername()),
            true,
            chatMessageTypeEntity,
            List.of(messageAttachmentEntity),
            List.of()
        );

        botNotificationService.sendBotNotificationToAdmins("""
            Пользователь %s создал запрос на проверку. Проверьте "Прямой заказ № %s" и примите запрос.
            """.formatted(fromUserEntity.getUsername(), forwardOrderEntity.getOrder().getTechNumber())
        );
    }

    public List<ReviewRequestProjection> findReviewRequestsBy(Long forwardOrderId) {
        return forwardOrderReviewRequestRepository.findReviewProjectionsByForwardOrder_Id(forwardOrderId);
    }

    @Transactional
    public void deleteForwardOrderReviewRequest(Long forwardOrderId, Long forwardOrderReviewRequestId, Long currentUserId) {
        UserEntity fromUserEntity = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found id: " + currentUserId));

        ForwardOrderEntity forwardOrderEntity = forwardOrderRepository.findById(forwardOrderId)
            .orElseThrow(() -> new RuntimeException("Invalid forward order id: " + forwardOrderId));

        forwardOrderReviewRequestRepository.deleteById(forwardOrderReviewRequestId);

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        ChatMessageEntity message = messageService.sendMessage(
            null,
            forwardOrderEntity.getChat(),
            "Пользователь %s удалил запрос на проверку.".formatted(fromUserEntity.getUsername()),
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );
    }

    @Transactional
    public void changeFileSubmissionStatus(Long forwardOrderId, Boolean allowSendFile, Long currentUserId) {
        UserEntity fromUserEntity = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found id: " + currentUserId));

        ForwardOrderEntity forwardOrderEntity = forwardOrderRepository.findById(forwardOrderId)
            .orElseThrow(() -> new RuntimeException("Invalid forward order id: " + forwardOrderId));

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        String allowText = allowSendFile ? "<span class=\"badge text-bg-success\">Разрешил</span>"
            : "<span class=\"badge text-bg-warning\">Запретил</span>";

        ChatMetadataEntity chatMetadata = forwardOrderEntity.getChat().getChatMetadata();
        chatMetadata.setAuthorCanSubmitFiles(allowSendFile);

        ChatMessageEntity message = messageService.sendMessage(
            null,
            forwardOrderEntity.getChat(),
            "Пользователь %s %s отправку в чат файлов.".formatted(fromUserEntity.getUsername(), allowText),
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );
    }

    @Transactional
    public void changePaymentStatus(Long forwardOrderId, Boolean isPaymentSend, Long currentUserId) {
        UserEntity fromUserEntity = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found id: " + currentUserId));

        ForwardOrderEntity forwardOrderEntity = forwardOrderRepository.findById(forwardOrderId)
            .orElseThrow(() -> new RuntimeException("Invalid forward order id: " + forwardOrderId));

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        String allowText = isPaymentSend ? "<span class=\"badge text-bg-success\">Заказ оплачен</span>"
            : "<span class=\"badge text-bg-warning\">Заказ не оплачен</span>";

        forwardOrderEntity.setIsPaymentSend(isPaymentSend);

        forwardOrderRepository.save(forwardOrderEntity);

        ChatMessageEntity message = messageService.sendMessage(
            null,
            forwardOrderEntity.getChat(),
            "Пользователь %s изменил статус оплаты на: %s".formatted(fromUserEntity.getUsername(), allowText),
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );
    }

    @Transactional
    public void sendRequestResultToChat(Long forwardOrderReviewRequestId) {
        UserEntity expertUser = userRepository.findById(ChatNames.EXPERT_USER_ID)
            .orElseThrow(() -> new RuntimeException("User not found id: " + ChatNames.EXPERT_USER_ID));

        ForwardOrderReviewRequestEntity requestEntity = forwardOrderReviewRequestRepository.findById(forwardOrderReviewRequestId)
            .orElseThrow(() -> new RuntimeException("Invalid forward order request id: " + forwardOrderReviewRequestId));

        ChatEntity chat = requestEntity.getForwardOrder()
            .getChat();

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        ChatMessageAttachmentEntity messageAttachmentEntity = new ChatMessageAttachmentEntity();
        messageAttachmentEntity.setAttachment(requestEntity.getReview().getReviewAttachment());

        ChatMessageEntity message = messageService.sendMessage(
            expertUser,
            chat,
            "Экспертный отдел представляет результат проверки работы.",
            false,
            chatMessageTypeEntity,
            List.of(messageAttachmentEntity),
            List.of()
        );
    }

    @Transactional
    public void notifyCustomerJoinToChat(ForwardOrderEntity forwardOrder) {
        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        messageService.sendMessage(
            null,
            forwardOrder.getChat(),
            """
                Заказчик присоединился к чату.
                Поприветствуйте его!
                
                *если нет обратной связи - обратитесь к администратору
                """,
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );

        messageService.sendMessage(
            null,
            forwardOrder.getAdminChat(),
            """
                Заказчик присоединился к чату.
                Поприветствуйте его!
                """,
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );
    }

    @Transactional
    public ChatMessageEntity sendCustomerTelegramMessageToChat(Long systemChatId, String text, Map<String, byte[]> files) {
        ChatEntity chatEntity = forwardOrderRepository.findForwardOrderChatByChatId(systemChatId)
            .orElseThrow(() -> new RuntimeException("Invalid system chat id: " + systemChatId));

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE_FROM_CUSTOMER.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        List<ChatMessageAttachmentEntity> attachments = files.entrySet().stream()
            .map(entry -> attachmentService.saveAttachment(entry.getKey(), entry.getValue()))
            .map(ChatMessageAttachmentEntity::of)
            .toList();

        ChatMessageEntity message = messageService.sendMessage(
            null,
            chatEntity,
            text,
            false,
            chatMessageTypeEntity,
            attachments,
            List.of()
        );

        taskScheduler.schedule(() -> applicationEventPublisher.publishEvent(new CheckMessageByAiEvent(message.getId())), plusSeconds(5));

        return message;
    }

    public Instant plusSeconds(int seconds) {
        return LocalDateTime.now()
            .plusSeconds(seconds)
            .atZone(ZoneId.of("Europe/Moscow"))
            .toInstant();
    }
}
