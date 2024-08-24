package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.OrderAttachmentDto;
import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.messenger.OrderParticipantDto;
import by.forward.forward_system.core.dto.rest.AddParticipantRequestDto;
import by.forward.forward_system.core.dto.ui.DeclineDto;
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
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    public OrderEntity update(Long id, OrderEntity order) {
        Optional<OrderEntity> byId = orderRepository.findById(id);
        OrderEntity orderEntity = byId.orElseThrow(() -> new RuntimeException("Order not found with id " + id));

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
        orderEntity.setTakingCost(order.getTakingCost());
        orderEntity.setAuthorCost(order.getAuthorCost());

        orderEntity = orderRepository.save(orderEntity);
        return orderEntity;
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

        Optional<ChatEntity> newOrdersChatByUser = chatRepository.findChatByUserAndChatName(userEntity.getId(), ChatNames.NEW_ORDER_CHAT_NAME.formatted(userEntity.getUsername()));
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
                "Поступил новый заказ №%s.\nНазвание работы \"%s\".\nТема \"%s\".\nДисциплина \"%s\".".formatted(techNumber, name, discipline, subject),
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
        for (OrderAttachment orderAttachment : orderEntity.getOrderAttachment()) {
            OrderAttachmentDto orderAttachmentDto = new OrderAttachmentDto();

            orderAttachmentDto.setId(orderAttachment.getId());
            orderAttachmentDto.setOrderId(orderEntity.getId());
            orderAttachmentDto.setAttachmentId(orderAttachment.getAttachment().getId());
            orderAttachmentDto.setAttachmentName(orderAttachment.getAttachment().getFilename());

            orderAttachmentDtos.add(orderAttachmentDto);
        }
        orderDto.setAttachments(orderAttachmentDtos);

        return orderDto;
    }

    public void acceptOrderByUser(Long orderId, Long currentUserId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        UserEntity userEntity = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));

        UserEntity catcherEntity = orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType().equals(ParticipantType.CATCHER))
            .findAny()
            .map(OrderParticipantEntity::getUser)
            .orElseThrow(() -> new RuntimeException("Participant CATCHER not found"));

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        chatService.createChat(
            Arrays.asList(userEntity, catcherEntity),
            ChatNames.ORDER_REQUEST_CHAT.formatted(orderEntity.getTechNumber(), userEntity.getUsername()),
            orderEntity,
            "Автор %s (%s) готов обсудить заказ.".formatted(userEntity.getFio(), userEntity.getUsername()),
            chatTypeEntity
        );
    }

    public void declineOrder(Long orderId, Long currentUserId, DeclineDto decline) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));
        UserEntity userEntity = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));

        OrderParticipantEntity orderParticipantUser = orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getUser().getId().equals(userEntity.getId()))
            .findAny().orElseThrow(() -> new RuntimeException("User not found with id " + userEntity.getId()));

        OrderParticipantsTypeEntity declineAuthorType = orderParticipantsTypeRepository.findById(ParticipantType.DECLINE_AUTHOR.getName())
            .orElseThrow(() -> new RuntimeException("Decline Author not found"));

        orderParticipantUser.setParticipantsType(declineAuthorType);
        orderParticipantRepository.save(orderParticipantUser);

        UserEntity catcherEntity = orderEntity.getOrderParticipants().stream()
            .filter(t -> t.getParticipantsType().getType().equals(ParticipantType.CATCHER))
            .findAny()
            .map(OrderParticipantEntity::getUser)
            .orElseThrow(() -> new RuntimeException("Participant CATCHER not found"));

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.REQUEST_ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        chatService.createChat(
            Collections.singletonList(catcherEntity),
            "Отказ от заказ №%s автором %s (%s)".formatted(orderEntity.getTechNumber(), userEntity.getFio(), userEntity.getUsername()),
            orderEntity,
            "Автор %s (%s) не готов взять в работу заказ. Причина: %s".formatted(userEntity.getFio(), userEntity.getUsername(), decline.getDeclineText()),
            chatTypeEntity
        );
    }

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
        List<UserEntity> experts = userRepository.findAllById(update.getExperts());
        List<UserEntity> catchers = userRepository.findAllById(update.getCatchers());
        List<UserEntity> hosts = userRepository.findAllById(update.getHosts());

        OrderParticipantsTypeEntity mainAuthorParticipant = orderParticipantsTypeRepository.findById(ParticipantType.MAIN_AUTHOR.getName()).orElseThrow(() -> new RuntimeException("Main Author not found"));
        for (UserEntity author : authors) {
            addParticipant(orderEntity, mainAuthorParticipant, author.getId());
        }

        OrderParticipantsTypeEntity expertParticipant = orderParticipantsTypeRepository.findById(ParticipantType.EXPERT.getName()).orElseThrow(() -> new RuntimeException("EXPERT not found"));
        for (UserEntity expert : experts) {
            addParticipant(orderEntity, expertParticipant, expert.getId());
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

        ChatTypeEntity chatTypeEntity = chatTypeRepository.findById(ChatType.ORDER_CHAT.getName()).orElseThrow(() -> new RuntimeException("Chat type not found"));
        chatService.createChat(
            orderParticipants,
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

    public UserEntity findExpert(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        for (OrderParticipantEntity orderParticipant : orderEntity.getOrderParticipants()) {
            if (orderParticipant.getParticipantsType().getType().equals(ParticipantType.EXPERT)) {
                return orderParticipant.getUser();
            }
        }
        throw new RuntimeException("Expert not found");
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
                    "В чат добавлен пользователь %s".formatted(userEntity.getFio()),
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

}
