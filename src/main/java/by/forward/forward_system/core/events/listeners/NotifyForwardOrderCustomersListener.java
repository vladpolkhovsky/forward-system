package by.forward.forward_system.core.events.listeners;

import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.events.events.NotifyForwardOrderCustomersEvent;
import by.forward.forward_system.core.jpa.model.BotIntegrationDataEntity;
import by.forward.forward_system.core.jpa.model.ChatMessageEntity;
import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.jpa.repository.CustomerTelegramToForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.utils.ChatNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class NotifyForwardOrderCustomersListener {

    private final MessageRepository messageRepository;
    private final ForwardOrderRepository forwardOrderRepository;
    private final CustomerTelegramToForwardOrderRepository customerTelegramToForwardOrderRepository;
    private final TelegramClient customerTelegramClient;

    @EventListener(NotifyForwardOrderCustomersEvent.class)
    @Transactional
    public void listen(NotifyForwardOrderCustomersEvent event) {
        Optional<ChatMessageEntity> messageById = messageRepository.findById(event.getChatMessageId());
        Optional<ForwardOrderEntity> forwardOrderId = forwardOrderRepository.findById(event.getForwardOrderId());

        messageById.filter(this::isNeedToBeSendToCustomer)
            .ifPresent(message -> {
                forwardOrderId.map(ForwardOrderEntity::getId)
                    .map(customerTelegramToForwardOrderRepository::findForwardOrderCustomers)
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(botIntegrationData -> sendMessageToCustomer(botIntegrationData, message));
            });
    }

    private boolean isNeedToBeSendToCustomer(ChatMessageEntity chatMessageEntity) {
        return chatMessageEntity.getFromUser() != null &&
               !chatMessageEntity.getIsSystemMessage() &&
               !chatMessageEntity.getChatMessageType().getType().equals(ChatMessageType.MESSAGE_FROM_CUSTOMER);
    }

    private void sendMessageToCustomer(BotIntegrationDataEntity botIntegrationData, ChatMessageEntity message) {
        try {
            SendMessage renderedMessage = renderMessage(SendMessage.builder(), message)
                .chatId(botIntegrationData.getTelegramChatId())
                .build();

            Message fakeNewMessage = customerTelegramClient.execute(renderedMessage);

            customerTelegramClient.execute(SendMessage.builder()
                .replyToMessageId(fakeNewMessage.getMessageId())
                .text("У вас новое сообщение в чате: *%s*".formatted(message.getChat().getChatName()))
                .parseMode("markdown")
                .chatId(botIntegrationData.getTelegramChatId())
                .build());

        } catch (Exception ex) {
            log.error("Ошибка отправки уведомления заказчику. Чат {}", message.getChat().getChatName(), ex);
        }
    }

    private SendMessage.SendMessageBuilder<?, ?> renderMessage(SendMessage.SendMessageBuilder<?, ?> builder, ChatMessageEntity message) {
        String messageContent = StringUtils.defaultIfBlank(message.getContent(), "<Сообщение без текста>");

        String username = Optional.ofNullable(message.getFromUser())
            .map(t -> {
                if (t.getId() <= ChatNames.ALWAYS_SHOW_NAME_ID_LIMIT) {
                    return t.getUsername();
                }
                if (t.hasAuthority(Authority.ADMIN)) {
                    return "Администратор";
                }
                return "Автор";
            }).orElse("<Анонимный пользователь>");

        String fakeNewMassage = """
            Сообщение от *%s*:
            %s
            """.formatted(username, messageContent);

        if (CollectionUtils.isNotEmpty(message.getChatMessageAttachments())) {
            fakeNewMassage += """
                
                <Также к чату прикреплены файлы.>
                """;
            List<InlineKeyboardRow> rows = message.getChatMessageAttachments().stream()
                .map(t -> new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text("Скачать " + t.getAttachment().getFilename())
                    .callbackData("/q-load-file " + t.getAttachment().getId())
                    .build()))
                .toList();
            builder.replyMarkup(new InlineKeyboardMarkup(rows));
        }

        return builder.text(fakeNewMassage)
            .parseMode("markdown");
    }
}
