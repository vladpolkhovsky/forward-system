package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.services.messager.BotNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ForwardSystemCustomerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Qualifier("customerTelegramToken")
    private final String customerTelegramToken;

    private final CustomerCommunicationProcessor customerCommunicationProcessor;

    private BotSession botSession;

    private final JoinToForwardChatCommandResolver joinToForwardChatCommandResolver;

    private final TelegramClient customerTelegramClient;

    @Override
    @SneakyThrows
    public void consume(Update update) {
        if (update.hasMessage()
            && update.getMessage().hasText()
            && joinToForwardChatCommandResolver.accept(update.getMessage().getText())) {
            joinToForwardChatCommandResolver.resolve(customerTelegramClient, update);
            return;
        }

        Optional<Long> chatIdMessage = Optional.ofNullable(update.getMessage()).map(Message::getChatId);
        Optional<Long> chatIdCallbackQuery = Optional.ofNullable(update.getCallbackQuery()).map(CallbackQuery::getMessage).map(MaybeInaccessibleMessage::getChatId);
        Optional<Long> chatId = Stream.concat(chatIdCallbackQuery.stream(), chatIdMessage.stream()).findAny();

        chatId.ifPresent(aLong -> customerCommunicationProcessor.process(update, aLong));
    }

    @Override
    public String getBotToken() {
        return customerTelegramToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        this.botSession = botSession;
    }

    @EventListener(ContextClosedEvent.class)
    @SneakyThrows
    public void onContextClosedEvent(ContextClosedEvent contextClosedEvent) {
        this.botSession.close();
    }
}
