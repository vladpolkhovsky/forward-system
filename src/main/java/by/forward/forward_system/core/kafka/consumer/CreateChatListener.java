package by.forward.forward_system.core.kafka.consumer;

import by.forward.forward_system.core.kafka.events.CreateChatEvent;
import by.forward.forward_system.core.services.messager.ChatCreatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChatListener {

    private final ChatCreatorService service;

    @RetryableTopic(attempts = "5", kafkaTemplate = "createChatEventKafkaTemplate", dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "${topic.create-chat-topic}", containerFactory = "createChatEventKafkaListenerContainerFactory")
    public void listen(@Payload CreateChatEvent event) {
        log.info("Processing create chat event: {}", event);
        service.createChat(event.getDto(), event.getInitialMessage(), event.getAddAdminAutomatically());
    }
}
