package by.forward.forward_system.core.bot;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@AllArgsConstructor
public class HelpCommandResolver implements CommandResolver {

    @Override
    public boolean accept(String command) {
        return command.startsWith("/help");
    }

    private static final String HELP_TEXT = """
        Доступны следующие комманды для бота.
        
        /reg <код> - регистрация в боте для показа уведомлений и возможности использовать следующие комманды.
        /stop-notification <chat-id> - отписка от уведомлений из чата
        /continue-notification <chat-id> - отмена отписки от уведомлений из чата
        /user-list - список всех зарегистрированных в боте пользователей
        """;

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(HELP_TEXT)
            .build());
    }
}
