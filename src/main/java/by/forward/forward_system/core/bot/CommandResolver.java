package by.forward.forward_system.core.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface CommandResolver {

    boolean accept(String command);

    void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException;

}
