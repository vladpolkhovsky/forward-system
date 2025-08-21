package by.forward.forward_system.core.kafka.producer;

import by.forward.forward_system.core.kafka.events.CreateChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChatProducer {

    @Value("${topic.create-chat-topic}")
    private String topic;

    private final KafkaTemplate<String, CreateChatEvent> kafkaTemplate;

    public void publish(CreateChatEvent createChatEvent) {
        log.info("Publishing CreateChatEvent to topic: {}, event: {}", topic, createChatEvent);
        kafkaTemplate.send(topic, createChatEvent);
    }
}
