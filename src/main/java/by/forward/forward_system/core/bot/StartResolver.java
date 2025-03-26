package by.forward.forward_system.core.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StartResolver implements CommandResolver {

    @Override
    public boolean accept(String command) {
        return command.startsWith("/start");
    }

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(TextHelper.REGISTER_HELP_TEXT)
            .build());
    }
}
