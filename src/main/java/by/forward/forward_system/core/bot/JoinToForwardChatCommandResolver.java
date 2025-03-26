package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.jpa.model.ForwardOrderEntity;
import by.forward.forward_system.core.services.core.ForwardOrderService;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Component
@AllArgsConstructor
public class JoinToForwardChatCommandResolver implements CommandResolver {

    private final BotNotificationService botNotificationService;

    private final ForwardOrderService forwardOrderService;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/join");
    }

    @Override
    @Transactional
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        String text = update.getMessage().getText();
        String[] args = text.split(" +");

        if (args.length != 2) {
            SendMessage build = SendMessage.builder()
                .text("Неправильный набор аргументов. Используйте: `/join <код>`")
                .chatId(update.getMessage().getChatId())
                .build();
            telegramClient.execute(build);
            return;
        }

        Optional<ForwardOrderEntity> byCode = botNotificationService.registerBotUserByForwardOrder(
            update.getMessage().getChatId(),
            update.getMessage().getFrom().getId(),
            args[1]
        );

        byCode.ifPresent(forwardOrderService::notifyCustomerJoinToChat);

        String answer = byCode.isPresent() ? "Вы успешно зарегистрировались!" : "Неправильный код!";

        telegramClient.execute(SendMessage.builder()
            .text(answer)
            .chatId(update.getMessage().getChatId())
            .build()
        );
    }
}
