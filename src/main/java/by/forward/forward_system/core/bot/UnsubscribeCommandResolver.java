package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.services.messager.BotNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@AllArgsConstructor
public class UnsubscribeCommandResolver implements CommandResolver {

    private final BotNotificationService botNotificationService;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/unsubscribe");
    }

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        botNotificationService.unsubscribe(update.getMessage().getChatId());

        telegramClient.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text("Вы отписаны от всех уведомлений.")
            .build());

        telegramClient.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(TextHelper.REGISTER_HELP_TEXT)
            .build());
    }
}
