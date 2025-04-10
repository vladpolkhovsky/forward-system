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

    private static final String HELP_TEXT = """
        Доступны следующие команды для бота.
        
        /reg <код> - регистрация в боте для показа уведомлений и возможности использовать следующие комманды.
        /stop_notification <chat-id> - отписка от уведомлений из чата
        /continue_notification <chat-id> - отмена отписки от уведомлений из чата
        /user_list - список всех зарегистрированных в боте пользователей
        /unsubscribe - отписаться от рассылки сообщений
        /help - помощь
        /chats - получить список доступных чатов
        """;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/help");
    }

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(HELP_TEXT)
            .build());
    }
}
