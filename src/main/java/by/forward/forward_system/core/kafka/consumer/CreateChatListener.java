package by.forward.forward_system.core.kafka.consumer;

import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.kafka.events.CreateChatEvent;
import by.forward.forward_system.core.services.messager.ChatCreatorService;
import by.forward.forward_system.core.services.messager.MessageService;
import jakarta.xml.bind.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChatListener {

    private final ChatCreatorService service;
    private final MessageService messageService;

    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(value = 7000L, maxDelay = 60000, multiplier = 2),
        kafkaTemplate = "createChatEventKafkaTemplate",
        exclude = {DataIntegrityViolationException.class}
    )
    @KafkaListener(topics = "${topic.create-chat-topic}", containerFactory = "createChatEventKafkaListenerContainerFactory")
    public void listen(@Payload CreateChatEvent event, Acknowledgment ack) {
        try {
            log.info("Processing create chat event: {}", event);

            ChatEntity result = service.createChat(event.getDto(), event.getInitialMessage(), event.getAddAdminAutomatically());

            log.info("Successfully processed chat creation event. Created chat ID: {}", result.getId());
            ack.acknowledge();
        } catch (DataIntegrityViolationException e) {
            // Бизнес-ошибки - подтверждаем сообщение, не повторяем
            log.error("Business error processing chat creation event: {}. Error: {}", event, e.getMessage(), e);
            messageService.sendMessageToErrorChat("ERROR: Ошибка обработки задания на создание чата: " + event
                + " Error: " + ExceptionUtils.getStackTrace(e));
            ack.acknowledge();
        } catch (Exception e) {
            // Технические ошибки - пробрасываем для retry
            log.error("Technical error processing chat creation event: {}", event, e);
            throw e;
        }
    }

    @KafkaListener(topics = "${topic.create-chat-topic}-dlt")
    public void listenDlt(@Payload CreateChatEvent event, Acknowledgment ack) {
        try {
            log.error("Event that failed processing after retries: {}", event);
            messageService.sendMessageToErrorChat("DLT: Ошибка обработки задания на создание чата: " + event);
        } catch (Exception e) {
            log.error("Technical error processing event: {}", event, e);
        }
        ack.acknowledge();
    }
}
