package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.OrderAttachmentDto;
import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.messenger.OrderParticipantDto;
import by.forward.forward_system.core.dto.rest.AddParticipantRequestDto;
import by.forward.forward_system.core.dto.ui.UpdateOrderRequestDto;
import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.ChatType;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjection;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.ui.UserUiService;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderParticipantsTypeRepository orderParticipantsTypeRepository;
    private final UserRepository userRepository;
    private final OrderParticipantRepository orderParticipantRepository;
    private final MessageService messageService;
    private final ChatRepository chatRepository;
    private final ChatMessageTypeRepository chatMessageTypeRepository;
    private final ChatService chatService;
    private final ChatTypeRepository chatTypeRepository;
    private final AttachmentService attachmentService;
    private final OrderAttachmentRepository orderAttachmentRepository;
    private final UserUiService userUiService;
    private final AIDetector aiDetector;
    private final BanService banService;
    private final UserService userService;

    public Optional<OrderEntity> getById(Long id) {
        return orderRepository.findById(id);
    }

    public List<OrderEntity> findAllOrder() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> findAllOrdersInStatus(List<String> orderStatuses) {
        return orderRepository.findByStatus(orderStatuses);
    }

    public OrderEntity save(OrderEntity orderEntity) {
        Optional<OrderStatusEntity> statusById = orderStatusRepository.findById(OrderStatus.CREATED.getName());
        OrderStatusEntity orderStatusEntity = statusById.orElseThrow(() -> new RuntimeException("OrderStatus not found with name " + OrderStatus.CREATED.getName()));
        orderEntity.setCreatedAt(LocalDateTime.now());
        orderEntity.setOrderStatus(orderStatusEntity);
        return orderRepository.save(orderEntity);
    }

    private String dateToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "не указано";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return localDateTime.format(formatter);
    }

    public OrderEntity update(Long id, OrderEntity order) {
        Optional<OrderEntity> byId = orderRepository.findById(id);
        OrderEntity orderEntity = byId.orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        Map<String, List<String>> changes = getChanges(orderEntity, order);

        orderEntity.setName(order.getName());
        orderEntity.setTechNumber(order.getTechNumber());
        orderEntity.setWorkType(order.getWorkType());
        orderEntity.setDiscipline(order.getDiscipline());
        orderEntity.setSubject(order.getSubject());
        orderEntity.setOriginality(order.getOriginality());
        orderEntity.setVerificationSystem(order.getVerificationSystem());
        orderEntity.setAdditionalDates(order.getAdditionalDates());
        orderEntity.setIntermediateDeadline(order.getIntermediateDeadline());
        orderEntity.setDeadline(order.getDeadline());
        orderEntity.setOther(order.getOther());
        orderEntity.setAuthorCost(order.getAuthorCost());
        orderEntity.setTakingCost(order.getTakingCost());

        String changesText = convertChangesToText(changes);
        sendChangesToChat(order.getId(), changesText);

        orderEntity = orderRepository.save(orderEntity);

        return orderEntity;
    }

    private Map<String, List<String>> getChanges(OrderEntity oldData, OrderEntity newData) {
        HashMap<String, List<String>> changes = new HashMap<>();
        if (!oldData.getName().equals(newData.getName())) {
            changes.put("Название", Arrays.asList(oldData.getName(), newData.getName()));
        }
        if (!oldData.getTechNumber().equals(newData.getTechNumber())) {
            changes.put("Номер ТЗ", Arrays.asList(oldData.getTechNumber(), newData.getTechNumber()));
        }
        if (!oldData.getWorkType().equals(newData.getWorkType())) {
            changes.put("Тип работы", Arrays.asList(oldData.getWorkType(), newData.getWorkType()));
        }
        if (!oldData.getDiscipline().equals(newData.getDiscipline())) {
            changes.put("Дисциплина", Arrays.asList(oldData.getDiscipline().getName(), newData.getDiscipline().getName()));
        }
        if (!oldData.getSubject().equals(newData.getSubject())) {
            changes.put("Предмет", Arrays.asList(oldData.getSubject(), newData.getSubject()));
        }
        if (!oldData.getOriginality().equals(newData.getOriginality())) {
            changes.put("Оригинальность", Arrays.asList(oldData.getOriginality().toString(), newData.getOriginality().toString()));
        }
        if (!oldData.getVerificationSystem().equals(newData.getVerificationSystem())) {
            changes.put("Система проверки", Arrays.asList(oldData.getVerificationSystem(), newData.getVerificationSystem()));
        }
        if (!oldData.getAdditionalDates().equals(newData.getAdditionalDates())) {
            changes.put("Дополнительные этапы сдачи", Collections.emptyList());
        }
        if (!oldData.getIntermediateDeadline().equals(newData.getIntermediateDeadline())) {
            changes.put("Промежуточный срок сдачи", Arrays.asList(dateToString(oldData.getIntermediateDeadline()), dateToString(newData.getIntermediateDeadline())));
        }
        if (!oldData.getDeadline().equals(newData.getDeadline())) {
            changes.put("Окончательный срок сдачи", Arrays.asList(dateToString(oldData.getDeadline()), dateToString(newData.getDeadline())));
        }
        if (!oldData.getOther().equals(newData.getOther())) {
            changes.put("Доп. информация", Arrays.asList(oldData.getVerificationSystem(), newData.getVerificationSystem()));
        }
        if (!oldData.getAuthorCost().equals(newData.getAuthorCost())) {
            changes.put("Цена", Arrays.asList(oldData.getAuthorCost().toString(), newData.getAuthorCost().toString()));
        }
        return changes;
    }

    private String convertChangesToText(Map<String, List<String>> changes) {
        return "Изменения в заказе:\n" + changes.entrySet().stream().map(entry -> {
            String text = "Поле " + entry.getKey() + ": ";
            if (entry.getValue().isEmpty()) {
                text = text + "невозможно отобразить см. описание заказа.";
            } else if (entry.getValue().size() == 1) {
                text = text + "новое значение: " + entry.getValue().get(0);
            } else if (entry.getValue().size() == 2) {
                text = text + "было \"" + entry.getValue().get(0) + "\", стало \"" + entry.getValue().get(1) + "\"";
            } else {
                return text + "невозможно отобразить см. описание заказа.";
            }
            return text;
        }).collect(Collectors.joining("\n"));
    }

    private void sendChangesToChat(Long orderId, String changesString) {
        Long chatId = getOrderMainChat(orderId);
        if (chatId == null) {
            return;
        }

        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow((() -> new RuntimeException("Chat not found")));
        ChatMessageTypeEntity messageType = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Message type not found"));

        messageService.sendMessage(null, chatEntity, changesString, true, messageType, Collections.emptyList(), Collections.emptyList());
    }

    public void addParticipant(Long orderId, AddParticipantRequestDto addParticipantRequestDto) {
        OrderEntity orderEntity = getById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        ParticipantType participantType = ParticipantType.byName(addParticipantRequestDto.role());
        if (participantType.equals(ParticipantType.AUTHOR)) {
            addAuthorParticipant(orderEntity, addParticipantRequestDto.ids());
        }
        if (participantType.equals(ParticipantType.CATCHER)) {
            addCatcherParticipant(orderEntity, addParticipantRequestDto.ids().get(0));
        }
    }

    private OrderEntity addCatcherParticipant(OrderEntity orderEntity, Long userId) {
        OrderParticipantsTypeEntity orderParticipantsTypeEntity = orderParticipantsTypeRepository.findById(ParticipantType.CATCHER.getName()).get();
        OrderParticipantEntity removeOrderParticipantEntity = null;

        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (orderParticipant.getParticipantsType().getType().equals(ParticipantType.CATCHER)) {
                removeOrderParticipantEntity = orderParticipant;
                break;
            }
        }

        if (removeOrderParticipantEntity != null) {
            orderEntity.getOrderParticipants().remove(removeOrderParticipantEntity);
            orderParticipantRepository.delete(removeOrderParticipantEntity);
        }

        addParticipant(orderEntity, orderParticipantsTypeEntity, userId);

        return orderRepository.save(orderEntity);
    }

    private void addAuthorParticipant(OrderEntity orderEntity, List<Long> userIds) {
        OrderParticipantsTypeEntity orderParticipantsTypeEntity = orderParticipantsTypeRepository.findById(ParticipantType.AUTHOR.getName()).get();

        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (isAssignedAuthor(orderParticipant)) {
                userIds.remove(orderParticipant.getUser().getId());
            }
        }

        for (Long userId : userIds) {
            addParticipant(orderEntity, orderParticipantsTypeEntity, userId);
            sendNewOrderRequest(
                userId,
                orderEntity.getId(),
                new BigDecimal(orderEntity.getTechNumber()),
                orderEntity.getName(),
                orderEntity.getDiscipline().getName(),
                orderEntity.getSubject()
            );
        }

        orderRepository.save(orderEntity);

        changeStatus(orderEntity.getId(), OrderStatus.CREATED, OrderStatus.DISTRIBUTION);
    }

    private boolean isAssignedAuthor(OrderParticipantEntity orderParticipant) {
        return orderParticipant.getParticipantsType().getType().equals(ParticipantType.AUTHOR)
            || orderParticipant.getParticipantsType().getType().equals(ParticipantType.DECLINE_AUTHOR);
    }

    private void sendNewOrderRequest(Long userId, Long orderId, BigDecimal techNumber, String name, String discipline, String subject) {
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        Long currentUserId = userUiService.getCurrentUserId();

        UserEntity manager = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));

        Optional<ChatEntity> newOrdersChatByUser = chatRepository.findChatByUserAndChatName(userEntity.getId(), ChatNames.NEW_ORDER_CHAT_NAME.formatted(userEntity.getUsername(), manager.getUsername()));
        Optional<ChatMessageTypeEntity> chatMessageType = chatMessageTypeRepository.findById(ChatMessageType.NEW_ORDER.getName());
        Optional<OrderParticipantsTypeEntity> participantsType = orderParticipantsTypeRepository.findById(ParticipantType.AUTHOR.getName());

        ChatMessageOptionEntity chatMessageOptionEntity = new ChatMessageOptionEntity();
        chatMessageOptionEntity.setOptionName("Рассмотреть предложение");
        chatMessageOptionEntity.setContent("/request-order/" + orderId);
        chatMessageOptionEntity.setOptionResolved(false);
        chatMessageOptionEntity.setOrderParticipant(participantsType.get());

        if (newOrdersChatByUser.isPresent() && chatMessageType.isPresent()) {
            ChatMessageEntity chatMessageEntity = messageService.sendMessage(
                null,
                newOrdersChatByUser.get(),
                "Поступил новый заказ №%s.\nНазвание работы \"%s\".\nТема \"%s\".\nДисциплина \"%s\".".formatted(techNumber, name, subject, discipline),
                true,
                chatMessageType.get(),
                Collections.emptyList(),
                Collections.singletonList(chatMessageOptionEntity)
            );
        }
    }

    private void addParticipant(OrderEntity orderEntity, OrderParticipantsTypeEntity orderParticipantsTypeEntity, Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        OrderParticipantEntity orderParticipantEntity = new OrderParticipantEntity();
        orderParticipantEntity.setUser(userEntity);
        orderParticipantEntity.setParticipantsType(orderParticipantsTypeEntity);
        orderParticipantEntity.setOrder(orderEntity);

        if (orderParticipantsTypeEntity.getType().equals(ParticipantType.MAIN_AUTHOR)) {
            orderParticipantEntity.setFee(orderEntity.getAuthorCost());
        }

        orderParticipantRepository.save(orderParticipantEntity);
        orderEntity.getOrderParticipants().add(orderParticipantEntity);
    }

    public void changeStatus(Long orderId, OrderStatus from, OrderStatus to) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        if (orderEntity.getOrderStatus().getStatus().equals(from)) {
            OrderStatusEntity orderStatusEntity = orderStatusRepository.findById(to.getName())
                .orElseThrow(() -> new RuntimeException("OrderStatus not found with name " + to.getName()));
            orderEntity.setOrderStatus(orderStatusEntity);
        }
        orderRepository.save(orderEntity);
    }

    public void changeStatus(Long orderId, OrderStatus to) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        OrderStatusEntity orderStatusEntity = orderStatusRepository.findById(to.getName())
            .orElseThrow(() -> new RuntimeException("OrderStatus not found with name " + to.getName()));
        orderEntity.setOrderStatus(orderStatusEntity);
        orderRepository.save(orderEntity);
    }

    public List<OrderDto> getUserOrders(Long userId) {
        List<OrderEntity> orderEntities = orderRepository.findOrdersWithUserInParticipant(userId);
        List<OrderDto> orderDtos = orderEntities.stream().map(this::toDto)
            .toList();
        return orderDtos;
    }


    public List<OrderDto> getAllOrders() {
        List<OrderEntity> orderEntities = orderRepository.findAll();
        List<OrderDto> orderDtos = orderEntities.stream().map(this::toDto)
            .toList();
        return orderDtos;
    }

    public OrderDto getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        return toDto(orderEntity);
    }

    public OrderDto toDto(OrderEntity orderEntity) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderEntity.getId());
        orderDto.setName(orderEntity.getName());
        orderDto.setTechNumber(new BigDecimal(orderEntity.getTechNumber()));
        orderDto.setOrderStatus(orderEntity.getOrderStatus().getStatus().getName());
        orderDto.setOrderStatusRus(orderEntity.getOrderStatus().getStatus().getRusName());
        orderDto.setWorkType(orderEntity.getWorkType());
        orderDto.setDiscipline(orderEntity.getDiscipline().getName());
        orderDto.setSubject(orderEntity.getSubject());
        orderDto.setOriginality(orderEntity.getOriginality());
        orderDto.setVerificationSystem(orderEntity.getVerificationSystem());
        orderDto.setAdditionalDates(orderEntity.getAdditionalDates());
        orderDto.setIntermediateDeadline(orderEntity.getIntermediateDeadline());
        orderDto.setDeadline(orderEntity.getDeadline());
        orderDto.setOther(orderEntity.getOther());
        orderDto.setAuthorCost(orderEntity.getAuthorCost());
        orderDto.setTakingCost(orderEntity.getTakingCost());
        orderDto.setCreatedAt(orderEntity.getCreatedAt());

        List<OrderParticipantDto> orderParticipantDtos = new ArrayList<>();
        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            OrderParticipantDto orderParticipantDto = new OrderParticipantDto();

            orderParticipantDto.setId(orderParticipant.getId());
            orderParticipantDto.setOrderId(orderEntity.getId());
            orderParticipantDto.setUserId(orderParticipant.getUser().getId());
            orderParticipantDto.setParticipantsType(orderParticipant.getParticipantsType().getType().getName());
            orderParticipantDto.setParticipantsTypeRus(orderParticipant.getParticipantsType().getType().getRusName());

            orderParticipantDtos.add(orderParticipantDto);
        }
        orderDto.setParticipants(orderParticipantDtos);

        List<OrderAttachmentDto> orderAttachmentDtos = new ArrayList<>();
        for (OrderAttachmentEntity orderAttachmentEntity : orderEntity.getOrderAttachment()) {
            OrderAttachmentDto orderAttachmentDto = new OrderAttachmentDto();

            orderAttachmentDto.setId(orderAttachmentEntity.getId());
            orderAttachmentDto.setOrderId(orderEntity.getId());
            orderAttachmentDto.setAttachmentId(orderAttachmentEntity.getAttachment().getId());
            orderAttachmentDto.setAttachmentName(orderAttachmentEntity.getAttachment().getFilename());

            orderAttachmentDtos.add(orderAttachmentDto);
        }
        orderDto.setAttachments(orderAttachmentDtos);

        return orderDto;
    }

    @Transactional
    public void applyUpdateOrderRequest(UpdateOrderRequestDto update) {
        if (!update.getIsAccepted()) {
            changeStatus(update.getOrderId(), OrderStatus.ADMIN_REVIEW, OrderStatus.DISTRIBUTION);
            return;
        }

        OrderEntity orderEntity = orderRepository.findById(update.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));

        orderParticipantRepository.deleteAll(orderEntity.getOrderParticipants());
        orderEntity.getOrderParticipants().clear();
        orderEntity = orderRepository.save(orderEntity);

        List<UserEntity> authors = userRepository.findAllById(update.getAuthors());
        List<UserEntity> catchers = userRepository.findAllById(update.getCatchers());
        List<UserEntity> hosts = userRepository.findAllById(update.getHosts());

        OrderParticipantsTypeEntity mainAuthorParticipant = orderParticipantsTypeRepository.findById(ParticipantType.MAIN_AUTHOR.getName()).orElseThrow(() -> new RuntimeException("Main Author not found"));
        for (UserEntity author : authors) {
            addParticipant(orderEntity, mainAuthorParticipant, author.getId());
        }

        OrderParticipantsTypeEntity catcherParticipant = orderParticipantsTypeRepository.findById(ParticipantType.CATCHER.getName()).orElseThrow(() -> new RuntimeException("CATCHER not found"));
        for (UserEntity catcher : catchers) {
            addParticipant(orderEntity, catcherParticipant, catcher.getId());
        }

        OrderParticipantsTypeEntity hostParticipant = orderParticipantsTypeRepository.findById(ParticipantType.HOST.getName()).orElseThrow(() -> new RuntimeException("HOST not found"));
        for (UserEntity host : hosts) {
            addParticipant(orderEntity, hostParticipant, host.getId());
        }

        List<ParticipantType> userTypeInChat = Arrays.asList(ParticipantType.CATCHER, ParticipantType.MAIN_AUTHOR, ParticipantType.HOST);
        List<UserEntity> orderParticipants = orderEntity.getOrderParticipants().stream()
            .filter(t -> userTypeInChat.contains(t.getParticipantsType().getType()))
            .map(OrderParticipantEntity::getUser)
            .toList();

        ArrayList<UserEntity> withAdmins = new ArrayList<>(orderParticipants);

        List<UserEntity> usersWithRoleAdmin = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        withAdmins.addAll(usersWithRoleAdmin);

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        chatService.createChat(
            withAdmins,
            ChatNames.ORDER_CHAT.formatted(orderEntity.getTechNumber()),
            orderEntity,
            "Заказ одобрен администратором. Можете начинать работу.",
            chatTypeEntity
        );

        changeStatus(update.getOrderId(), OrderStatus.ADMIN_REVIEW, OrderStatus.IN_PROGRESS);
    }

    public List<ChatAttachmentProjection> getOrderMainChatAttachments(Long orderId) {
        return orderRepository.findChatAttachmentsByOrderId(orderId);
    }

    public Long getOrderMainChat(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        for (ChatEntity chat : orderEntity.getChats()) {
            if (chat.getChatType().getType().equals(ChatType.ORDER_CHAT)) {
                return chat.getId();
            }
        }
        return null;
    }

    public void addMainAuthorToOrder(Long orderId, Long authorId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        UserEntity userEntity = userRepository.findById(authorId).orElseThrow(() -> new RuntimeException("User not found"));

        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (orderParticipant.getUser().getId().equals(authorId)) {
                return;
            }
        }

        OrderParticipantsTypeEntity orderParticipantsTypeEntity = orderParticipantsTypeRepository.findById(ParticipantType.MAIN_AUTHOR.getName())
            .orElseThrow(() -> new RuntimeException("Main Author not found"));

        OrderParticipantEntity orderParticipant = new OrderParticipantEntity();
        orderParticipant.setOrder(orderEntity);
        orderParticipant.setUser(userEntity);
        orderParticipant.setFee(0);
        orderParticipant.setParticipantsType(orderParticipantsTypeEntity);

        orderParticipant = orderParticipantRepository.save(orderParticipant);
        orderEntity.getOrderParticipants().add(orderParticipant);

        orderEntity = orderRepository.save(orderEntity);

        ChatMessageTypeEntity messageType = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Message type not found"));
        for (ChatEntity chat : orderEntity.getChats()) {
            if (chat.getChatType().getType().equals(ChatType.ORDER_CHAT)) {
                chatService.addUserToChats(Collections.singletonList(chat.getId()), authorId);
                messageService.sendMessage(
                    null,
                    chat,
                    "В чат добавлен пользователь %s (%s)".formatted(userEntity.getFio(), userEntity.getUsername()),
                    true,
                    messageType,
                    Collections.emptyList(),
                    Collections.emptyList()
                );
            }
        }
    }

    public void applyFee(Long orderId, Map<Long, Integer> authorIdToFee) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (authorIdToFee.containsKey(orderParticipant.getUser().getId())) {
                orderParticipant.setFee(authorIdToFee.get(orderParticipant.getUser().getId()));
                orderParticipantRepository.save(orderParticipant);
            }
        }
    }

    public Integer countNotClosed() {
        return orderRepository.countAllByOrderStatusIsNot(OrderStatus.CLOSED.getName());
    }

    public Integer countFinalStatusOrders() {
        return orderRepository.countAllByOrderStatusIn(Arrays.asList(OrderStatus.GUARANTEE.getName(), OrderStatus.FINALIZATION.getName()));
    }

    public String getLastTechNumber() {
        return orderRepository.maxTechNumber().orElse("0");
    }

    public void checkOrderAccessEdit(Long orderId, Long currentUserId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        UserEntity userEntity = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userEntity.getAuthorities().contains(Authority.ADMIN) || userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }

        if (orderEntity.getCreatedBy().getId().equals(userEntity.getId())) {
            return;
        }

        List<ParticipantType> editTypes = Arrays.asList(ParticipantType.HOST, ParticipantType.CATCHER);
        boolean isParticipant = orderEntity.getOrderParticipants()
            .stream()
            .anyMatch(t -> t.getUser().getId().equals(currentUserId) && editTypes.contains(t.getParticipantsType().getType()));

        if (isParticipant) {
            return;
        }

        throw new IllegalStateException("У вас нет доступа к данному заказу.");
    }

    public void checkOrderAccessView(Long orderId, Long currentUserId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        UserEntity userEntity = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userEntity.getAuthorities().contains(Authority.ADMIN) || userEntity.getAuthorities().contains(Authority.OWNER)) {
            return;
        }

        if (orderEntity.getCreatedBy().getId().equals(userEntity.getId())) {
            return;
        }

        boolean isParticipant = orderEntity.getOrderParticipants()
            .stream()
            .anyMatch(t -> t.getUser().getId().equals(currentUserId));

        if (isParticipant) {
            return;
        }

        throw new IllegalStateException("У вас нет доступа к данному заказу.");
    }

    public Integer countMyOrders(Long currentUserId) {
        return orderRepository.countMyOrders(currentUserId);
    }

    @SneakyThrows
    public boolean saveOrderFile(Long id, MultipartFile[] file) {
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));


        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            AttachmentEntity attachmentEntity = attachmentService.saveAttachment(multipartFile.getOriginalFilename(), multipartFile.getBytes());
            attachmentEntities.add(attachmentEntity);
        }

        boolean iaApproved = true;
        List<Long> logIds = new ArrayList<>();
        for (AttachmentEntity attachmentEntity : attachmentEntities) {
            AIDetector.AICheckResult checkResult = aiDetector.isValidFile(userUiService.getCurrentUser().getUsername(), attachmentEntity.getId());
            if (!checkResult.isOk()) {
                logIds.add(checkResult.aiLogId());
            }
            iaApproved &= checkResult.isOk();
        }

        if (!iaApproved) {
            String aiLog = logIds.stream().map(t -> "<a href=\"/ai-log/%d\" target=\"_blank\">Лог проверки</a>".formatted(t)).collect(Collectors.joining(" "));
            String urls = attachmentEntities.stream().map(AttachmentEntity::getId).map(t -> "<a href=\"/load-file/%d\" target=\"_blank\">Файл</a>".formatted(t)).collect(Collectors.joining(" "));
            boolean isBanned = banService.ban(userUiService.getCurrentUserId(), "Файлы, прикреплённые к заказу, содержат личную информацию: %s \nЛог проверки: %s".formatted(urls, aiLog));
            if (isBanned) {
                return false;
            }
        }

        for (AttachmentEntity attachmentEntity : attachmentEntities) {
            OrderAttachmentEntity orderAttachmentEntity = new OrderAttachmentEntity();
            orderAttachmentEntity.setOrder(orderEntity);
            orderAttachmentEntity.setAttachment(attachmentEntity);

            orderAttachmentRepository.save(orderAttachmentEntity);
        }

        return true;
    }

    public List<OrderAttachmentDto> getOrderAttachments(Long id) {
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return toDto(orderEntity).getAttachments();
    }

    public void removeOrderFile(Long id, Long attachmentId) {
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        for (OrderAttachmentEntity orderAttachmentEntity : orderEntity.getOrderAttachment()) {
            if (orderAttachmentEntity.getAttachment().getId().equals(attachmentId)) {
                orderAttachmentRepository.delete(orderAttachmentEntity);
                return;
            }
        }
    }

    @Transactional
    public void delMainAuthorFromOrder(Long orderId, Long authorId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        UserEntity userEntity = userRepository.findById(authorId).orElseThrow(() -> new RuntimeException("User not found"));

        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (orderParticipant.getUser().getId().equals(authorId)) {
                orderParticipantRepository.delete(orderParticipant);
                break;
            }
        }

        ChatMessageTypeEntity messageType = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName())
            .orElseThrow(() -> new RuntimeException("Chat message type not found"));

        ChatEntity chat = chatRepository.findById(getOrderMainChat(orderId)).orElseThrow(() -> new RuntimeException("Order main chat not found"));

        chatService.delUserFromChats(Collections.singletonList(chat.getId()), authorId);

        chat = chatRepository.findById(getOrderMainChat(orderId)).orElseThrow(() -> new RuntimeException("Order main chat not found"));

        messageService.sendMessage(
            null,
            chat,
            "Из чата удалён пользователь %s (%s)".formatted(userEntity.getFio(), userEntity.getUsername()),
            true,
            messageType,
            Collections.emptyList(),
            Collections.emptyList()
        );

        chatService.setMessageViewed(chat.getId(), authorId);
    }

}
