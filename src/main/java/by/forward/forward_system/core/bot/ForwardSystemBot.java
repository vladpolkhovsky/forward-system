package by.forward.forward_system.core.bot;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@AllArgsConstructor
public class ForwardSystemBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Qualifier("telegramToken")
    private final String telegramToken;

    private final TelegramClient telegramClient;

    private final List<CommandResolver> commandResolverList;

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
                .text("Неправильная комманда. \nПожалуста зарегистрируйтесь используя комманду: \n/register <Код> или /reg <Код>. \nНапример: /reg 123456")
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
}
