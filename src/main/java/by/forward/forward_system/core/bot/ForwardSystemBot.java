package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.jpa.repository.CustomerTelegramToForwardOrderRepository;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ForwardSystemBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Qualifier("telegramToken")
    private final String telegramToken;

    private final TelegramClient telegramClient;

    private final List<CommandResolver> commandResolverList;
    private final CustomerTelegramToForwardOrderRepository customerTelegramToForwardOrderRepository;
    private final CustomerCommunicationProcessor customerCommunicationProcessor;

    private BotSession botSession;

    @Override
    public void consume(Update update) {
        boolean isAnyResolverAccept = false;

        for (CommandResolver commandResolver : commandResolverList) {
            boolean isAccepted = commandResolver.accept(update.getMessage().getText());
            if (isAccepted) {
                try {
                    commandResolver.resolve(telegramClient, update);
                } catch (TelegramApiException ignored) {

                }
            }
            isAnyResolverAccept |= isAccepted;
        }

        if (!isAnyResolverAccept) {
            SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(TextHelper.REGISTER_HELP_TEXT_ERROR)
                .build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException ignored) {

            }
        }
    }

    @Override
    public String getBotToken() {
        return telegramToken;
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
