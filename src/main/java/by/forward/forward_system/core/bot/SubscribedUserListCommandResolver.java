package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.jpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@AllArgsConstructor
public class SubscribedUserListCommandResolver implements CommandResolver {

    private final UserRepository userRepository;

    @Override
    public boolean accept(String command) {
        return command.startsWith("/user-list");
    }

    @Override
    public void resolve(TelegramClient telegramClient, Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();

        List<String> allUsernamesRegisteredInBot = userRepository.findAllUsernamesRegisteredInBot();

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < allUsernamesRegisteredInBot.size(); i++) {
            text.append(allUsernamesRegisteredInBot.get(i)).append("\n");
            if (i % 100 == 0) {
                telegramClient.execute(SendMessage.builder()
                    .text(text.toString())
                    .chatId(chatId)
                    .build());
                text = new StringBuilder();
            }
        }

        telegramClient.execute(SendMessage.builder()
            .text(text.toString())
            .chatId(chatId)
            .build());
    }
}
