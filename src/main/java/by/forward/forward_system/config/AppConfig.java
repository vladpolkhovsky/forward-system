package by.forward.forward_system.config;

import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.services.messager.MessageCounter;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.ui.UserUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean("newMsgCount")
    public MessageCounter newMsgCount(@Autowired MessageRepository messageRepository, @Autowired UserUiService userUiService) {
        return new MessageCounter(messageRepository, userUiService);
    }

}
