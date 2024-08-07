package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MessageCounter {

    private final MessageRepository messageRepository;

    private final UserUiService userUiService;

    public Integer newMessageCount() {
        return messageRepository.getNewMessageByUserId(userUiService.getCurrentUserId());
    }

}
