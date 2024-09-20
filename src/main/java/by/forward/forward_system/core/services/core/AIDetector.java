package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.ChatMessageType;
import by.forward.forward_system.core.jpa.model.AiLogEntity;
import by.forward.forward_system.core.jpa.model.ChatEntity;
import by.forward.forward_system.core.jpa.model.ChatMessageTypeEntity;
import by.forward.forward_system.core.jpa.repository.AiLogRepository;
import by.forward.forward_system.core.jpa.repository.ChatMessageTypeRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.utils.ChatNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class AIDetector {

    @Value("${ai.url}")
    private String aiUrl;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private AiLogRepository aiLogRepository;

    private static final String FILE_CHECK_PATH = "/check_file";
    private static final String MESSAGE_CHECK_PATH = "/check_message";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WebClient webClient;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMessageTypeRepository chatMessageTypeRepository;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder()
            .baseUrl(aiUrl)
            .build();
    }

    public AICheckResult isValidMessage(String message, String username) {
        MessageCheckRequest messageCheckRequest = new MessageCheckRequest(username, message);

        try {
            return sendToAi(messageCheckRequest, MESSAGE_CHECK_PATH, null);
        } catch (Exception exception) {
            Long logId = saveToIntegrationTable(messageCheckRequest, null, ExceptionUtils.getStackTrace(exception), null);
            sendMessageToErrorChat(logId);
        }

        return new AICheckResult(true, 0);
    }

    public AICheckResult isValidFile(String username, Long attachmentId) {
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

        ChatEntity chat = chatRepository.findById(ChatNames.ERRORS_CHAT_ID).orElseThrow(() -> new RuntimeException("Chat not found"));
        ChatMessageTypeEntity chatMessageType = chatMessageTypeRepository.findById(ChatMessageType.MESSAGE.getName()).orElseThrow(() -> new RuntimeException("message type not found"));

        messageService.sendMessage(null,
            chat,
            message,
            true,
            chatMessageType,
            Collections.emptyList(),
            Collections.emptyList()
        );
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
                                       String message) {

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
