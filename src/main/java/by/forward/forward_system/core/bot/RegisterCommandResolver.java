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
public class RegisterCommandResolver implements CommandResolver {

    private final BotNotificationService botNotificationService;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/register") || command.startsWith("/reg");
    }

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        String text = update.getMessage().getText();
        String[] args = text.split(" +");

        if (args.length != 2) {
            SendMessage build = SendMessage.builder()
                .text(TextHelper.REGISTER_HELP_TEXT_ERROR)
                .chatId(update.getMessage().getChatId())
                .build();
            telegramClient.execute(build);
            return;
        }

        String code = args[1];

        boolean isOkCode = botNotificationService.registerBotUser(
            update.getMessage().getChatId(),
            update.getMessage().getFrom().getId(),
            code
        );

        String answer = isOkCode ? "Вы успешно зарегистрировались!" : "Неправильный код!";

        telegramClient.execute(SendMessage.builder()
            .text(answer)
            .chatId(update.getMessage().getChatId())
            .build()
        );
    }

}
