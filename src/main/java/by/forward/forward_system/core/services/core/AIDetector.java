package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.model.AiLogEntity;
import by.forward.forward_system.core.jpa.repository.AiLogRepository;
import by.forward.forward_system.core.jpa.repository.ChatMessageTypeRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.services.messager.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class AIDetector {

    private static final String FILE_CHECK_PATH = "/check_file";
    private static final String MESSAGE_CHECK_PATH = "/check_message";
    private static final boolean ENABLE_FILE_CHECK = false;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.url}")
    private String aiUrl;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private AiLogRepository aiLogRepository;

    @Autowired
    private MessageService messageService;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder()
            .baseUrl(aiUrl)
            .build();
    }

    @Transactional
    public AICheckResult isValidMessage(String message, String username, String target) {
        MessageCheckRequest messageCheckRequest = new MessageCheckRequest(username, message, target);

        try {
            return sendToAi(messageCheckRequest, MESSAGE_CHECK_PATH, null);
        } catch (Exception exception) {
            Long logId = saveToIntegrationTable(messageCheckRequest, null, ExceptionUtils.getStackTrace(exception), null);
            sendMessageToErrorChat(logId);
        }

        return new AICheckResult(true, 0);
    }

    @Transactional
    public AICheckResult isValidFile(String username, Long attachmentId) {
        if (!ENABLE_FILE_CHECK) {
            return new AICheckResult(true, 0);
        }

        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(attachmentId);

        int i = attachmentFile.filename().lastIndexOf(".");
        String extension = attachmentFile.filename().substring(i + 1);

        boolean okFileExtension = isOkFileExtension(extension);

        if (!okFileExtension) {
            return new AICheckResult(true, 0);
        }

        FileCheckRequest fileCheckRequest = new FileCheckRequest(username,
            attachmentFile.filename(),
            extension,
            toBase64(attachmentFile.content())
        );

        try {
            return sendToAi(fileCheckRequest, FILE_CHECK_PATH, attachmentId);
        } catch (Exception exception) {
            Long logId = saveToIntegrationTable(fileCheckRequest, null, ExceptionUtils.getStackTrace(exception), attachmentId);
            sendMessageToErrorChat(logId);
        }

        return new AICheckResult(true, 0);
    }

    private AICheckResult sendToAi(Object request, String path, Long attachmentId) {
        AIResponse response = webClient.post()
            .uri(path)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AIResponse.class)
            .block();

        log.info("AI: Проверил сообщение. request={}, response={}", request, response);

        Long logId = saveToIntegrationTable(request, response, null, attachmentId);

        if (response != null && response.status != null && response.status.equals("ok")) {
            return new AICheckResult(true, logId);
        }

        return new AICheckResult(false, logId);
    }

    @SneakyThrows
    private Long saveToIntegrationTable(Object request, AIResponse response, String stacktrace, Long fileId) {
        if (request instanceof FileCheckRequest req) {
            request = new FileCheckRequest(req.username, req.filename, req.doc_type, "base 64 file content");
        }
        AiLogEntity aiLogEntity = new AiLogEntity();
        aiLogEntity.setRequestJson(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        aiLogEntity.setResponseJson(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        aiLogEntity.setFileId(fileId);
        aiLogEntity.setErrorText(StringUtils.abbreviate(stacktrace, 65000));
        aiLogEntity.setCreatedAt(LocalDateTime.now());
        aiLogEntity = aiLogRepository.save(aiLogEntity);
        return aiLogEntity.getId();
    }

    private void sendMessageToErrorChat(Long logId) {
        String message = """
            Произошла ошибка при обращении к сервису проверки сообщений. 
            <a href="/ai-log/%d" target="_blank">Лог</a>
            """.formatted(logId);

        messageService.sendMessageToErrorChat(message);
    }

    private String toBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    private boolean isOkFileExtension(String extension) {
        List<String> exList = Arrays.asList("pdf", "docx", "xlsx", "txt");
        return exList.contains(extension.toLowerCase());
    }

    public record AICheckResult(boolean isOk,
                                long aiLogId) {

    }

    private record MessageCheckRequest(String username,
                                       String message,
                                       String target) {

    }

    private record FileCheckRequest(String username,
                                    String filename,
                                    String doc_type,
                                    String data) {

    }

    private record AIResponse(String status,
                              String explanation) {

    }

}
