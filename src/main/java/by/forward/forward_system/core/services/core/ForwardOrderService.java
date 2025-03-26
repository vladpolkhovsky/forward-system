package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.ChatMessageType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ForwardOrderService {

    private static DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm");

    private final ForwardOrderRepository forwardOrderRepository;

    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ForwardOrderReviewRequestRepository forwardOrderReviewRequestRepository;

    private final MessageService messageService;
    private final ChatMessageTypeRepository chatMessageTypeRepository;
    private final BotNotificationService botNotificationService;
    private final ChatMetadataRepository chatMetadataRepository;

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

        Long fileId = attachmentService.saveAttachmentRaw(
            "Запрос на проверку ТЗ %s От %s Файл %s".formatted(
                forwardOrderEntity.getOrder().getTechNumber(),
                LocalDateTime.now().format(outputDateTimeFormat),
                StringUtils.abbreviateMiddle(file.getOriginalFilename(), "-", 10)
            ),
            file.getBytes()
        );

        AttachmentEntity attachmentEntity = attachmentRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found id: " + fileId));

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
    public void changePaymentStatus(Long forwardOrderId, Boolean allowSendFile, Long currentUserId) {
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

        chatMetadataRepository.save(chatMetadata);

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
            "Заказчик присоединился к чату. <strong>Сообщения до этого он не увидит.</strong> Уточните детали работы.",
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );

        messageService.sendMessage(
            null,
            forwardOrder.getAdminChat(),
            "Заказчик присоединился к чату.",
            true,
            chatMessageTypeEntity,
            List.of(),
            List.of()
        );
    }

    @Transactional
    public void sendCustomerTelegramMessageToChat(Long systemChatId, Long telegramChatId, String text, Map<String, byte[]> files) {
        ChatEntity chatEntity = forwardOrderRepository.findForwardOrderChatByChatId(systemChatId)
            .orElseThrow(() -> new RuntimeException("Invalid system chat id: " + systemChatId));

        ChatMessageTypeEntity chatMessageTypeEntity = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE_FROM_CUSTOMER.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        List<ChatMessageAttachmentEntity> attachments = files.entrySet().stream()
            .map(entry -> attachmentService.saveAttachmentRaw(entry.getKey(), entry.getValue()))
            .map(attachmentRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ChatMessageAttachmentEntity::of)
            .toList();

        messageService.sendMessage(
            null,
            chatEntity,
            text,
            false,
            chatMessageTypeEntity,
            attachments,
            List.of()
        );
    }
}
