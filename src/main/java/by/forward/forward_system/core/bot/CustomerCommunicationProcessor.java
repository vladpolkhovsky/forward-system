package by.forward.forward_system.core.bot;

import by.forward.forward_system.core.jpa.model.BotIntegrationDataEntity;
import by.forward.forward_system.core.jpa.repository.BotIntegrationDataRepository;
import by.forward.forward_system.core.jpa.repository.ChatRepository;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.ForwardOrderProjection;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.ForwardOrderService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class CustomerCommunicationProcessor {

    private static final Integer FILE_SIZE_LIMIT = 50 * 1024 * 1024;
    private static final Integer FILE_MEGABYTE = 1024 * 1024;

    private final TelegramClient telegramClient;
    private final BotIntegrationDataRepository botIntegrationDataRepository;
    private final ForwardOrderRepository forwardOrderRepository;
    private final ChatRepository chatRepository;
    private final ForwardOrderService forwardOrderService;

    @Qualifier("telegramToken")
    private final String telegramToken;
    private final AttachmentService attachmentService;

    @SneakyThrows
    @Transactional
    public void process(Update update, Long chatId) {

        BotIntegrationDataEntity botIntegrationData = botIntegrationDataRepository.findFirstByTelegramChatId(chatId)
            .orElseThrow(() -> new RuntimeException("Cant find BotIntegrationDataEntity chat id = " + chatId));

        if (update.hasCallbackQuery()) {
            processCallbackQuery(update, botIntegrationData);
            return;
        }

        if (!update.hasMessage()) {
            telegramClient.execute(SendMessage.builder()
                .text("Мы не поняли ваше сообщение. Бот не поддерживает удаление и исправление сообщений :(")
                .chatId(update.getMessage().getChatId())
                .build()
            );
            return;
        }

        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            processCommand(update.getMessage(), botIntegrationData);
            return;
        }

        handleUserMessage(update.getMessage(), botIntegrationData);
    }

    @SneakyThrows
    private void handleUserMessage(Message message, BotIntegrationDataEntity botIntegrationData) {
        telegramClient.execute(SendMessage.builder()
            .replyToMessageId(message.getMessageId())
            .text("В какой чат отправить данное сообщение?")
            .replyMarkup(replyMarkupForChatSelection(botIntegrationData, message.getMessageId().longValue()))
            .chatId(botIntegrationData.getTelegramChatId())
            .build()
        );
    }

    private void processCommand(Message message, BotIntegrationDataEntity botIntegrationData) {

    }

    @SneakyThrows
    private void processCallbackQuery(Update update, BotIntegrationDataEntity botIntegrationData) {
        if (update.getCallbackQuery().getData().startsWith("/q-send-message-to ")) {
            processSendMessageToChat(update, botIntegrationData);
            return;
        }

        if (update.getCallbackQuery().getData().startsWith("/q-load-file ")) {
            processFileRequest(update, botIntegrationData);
            return;
        }

        telegramClient.execute(SendMessage.builder()
            .text("Не понял команды: `update.getCallbackQuery().getData() -> %s`".formatted(update.getCallbackQuery().getData()))
            .chatId(botIntegrationData.getTelegramChatId())
            .build());
    }

    @SneakyThrows
    private void processFileRequest(Update update, BotIntegrationDataEntity botIntegrationData) {
        String[] split = update.getCallbackQuery().getData().split(" +");
        Long fileId = Long.parseLong(split[1]);

        AttachmentService.AttachmentFile attachmentFile = attachmentService.loadAttachment(fileId);

        telegramClient.execute(SendDocument.builder()
            .chatId(botIntegrationData.getTelegramChatId())
            .caption("Файл `%s`".formatted(attachmentFile.filename()))
            .document(new InputFile(new ByteArrayInputStream(attachmentFile.content()), attachmentFile.filename()))
            .build());
    }

    private void processSendMessageToChat(Update update, BotIntegrationDataEntity botIntegrationData) throws TelegramApiException {
        String[] split = update.getCallbackQuery().getData().split(" +");
        Long systemChatId = Long.parseLong(split[1]);
        Integer telegramMessageId = Integer.parseInt(split[2]);

        MaybeInaccessibleMessage messageAskToSendToChat = update.getCallbackQuery()
            .getMessage();

        Message messageCopy = telegramClient.execute(ForwardMessage.builder()
            .chatId(botIntegrationData.getTelegramChatId())
            .messageId(telegramMessageId)
            .fromChatId(botIntegrationData.getTelegramChatId())
            .build()
        );

        String text = StringUtils.defaultString(messageCopy.getText(), "") + StringUtils.defaultString(messageCopy.getCaption(), "");
        List<PhotoSize> photo = messageCopy.getPhoto();
        Document document = messageCopy.getDocument();
        Video video = messageCopy.getVideo();

        Map<String, String> fileIdToName = new HashMap<>();

        List<String> photosFileIds = CollectionUtils.emptyIfNull(photo).stream().max(Comparator.comparing(PhotoSize::getFileSize))
            .stream()
            .filter(t -> {
                if (t.getFileSize() > FILE_SIZE_LIMIT) {
                    notifyLargeFile(t.getFileId(), t.getFileSize().longValue(), botIntegrationData.getTelegramChatId());
                    return false;
                }
                fileIdToName.put(t.getFileId(), t.getFileId() + ".jpg");
                return true;
            })
            .map(PhotoSize::getFileId)
            .toList();

        List<String> documentFileIds = Optional.ofNullable(document)
            .filter(t -> {
                if (t.getFileSize() > FILE_SIZE_LIMIT) {
                    notifyLargeFile(t.getFileName(), t.getFileSize(), botIntegrationData.getTelegramChatId());
                    return false;
                }
                fileIdToName.put(t.getFileId(), t.getFileName());
                return true;
            })
            .map(Document::getFileId)
            .map(List::of)
            .orElse(List.of());

        List<String> videoFielIds = Optional.ofNullable(video)
            .filter(t -> {
                if (t.getFileSize() > FILE_SIZE_LIMIT) {
                    notifyLargeFile(t.getFileName(), t.getFileSize(), botIntegrationData.getTelegramChatId());
                    return false;
                }
                fileIdToName.put(t.getFileId(), t.getFileName());
                return true;
            })
            .map(Video::getFileId)
            .map(List::of)
            .orElse(List.of());

        ArrayList<String> singularObjectFileIds = new ArrayList<>() {{
            addAll(videoFielIds);
            addAll(documentFileIds);
        }};

        List<String> fileIds = Stream.concat(photosFileIds.stream(), singularObjectFileIds.stream()).toList();

        Map<String, byte[]> files = fileIds.stream()
            .map(fileId -> downloadFile(fileId, fileIdToName))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (StringUtils.isBlank(text) && files.isEmpty()) {
            telegramClient.execute(SendMessage.builder()
                .chatId(botIntegrationData.getTelegramChatId())
                .text("Мы не поняли что нужно отправить. Данный тип сообщения не поддерживается :(")
                .build());
            telegramClient.execute(SendMessage.builder()
                .chatId(botIntegrationData.getTelegramChatId())
                .text("Вы можете отправлять текст, картинки, документы и видео.")
                .build());
            return;
        }

        forwardOrderService.sendCustomerTelegramMessageToChat(systemChatId, botIntegrationData.getTelegramChatId(), text, files);

        telegramClient.execute(SendMessage.builder()
            .chatId(botIntegrationData.getTelegramChatId())
            .text("Сообщение отправлено в чат **%s**".formatted(chatRepository.findChatNameById(systemChatId).get()))
            .replyToMessageId(telegramMessageId)
            .parseMode("markdown")
            .build());

        telegramClient.execute(DeleteMessage.builder()
            .chatId(botIntegrationData.getTelegramChatId())
            .messageId(messageAskToSendToChat.getMessageId())
            .build()
        );

        telegramClient.execute(DeleteMessage.builder()
            .chatId(botIntegrationData.getTelegramChatId())
            .messageId(messageCopy.getMessageId())
            .build()
        );
    }

    @SneakyThrows
    private void notifyLargeFile(String filename, Long fileSize, Long telegramChatId) {
        telegramClient.execute(SendMessage.builder()
            .chatId(telegramChatId)
            .text("Файл `%s` слишком большой (%d мб). Наше ограничение на загрузку файлов - 50 мб. Используйте архив или пришлите ссылку на файл в облаке."
                .formatted(filename, fileSize / FILE_MEGABYTE)
            )
            .build()
        );
    }

    @SneakyThrows
    private Map.Entry<String, byte[]> downloadFile(String fileId, Map<String, String> fileIdToName) {
        GetFile getFile = new GetFile(fileId);
        File file = telegramClient.execute(getFile);
        String fileUrl = file.getFileUrl(telegramToken);
        byte[] byteArray = IOUtils.toByteArray(new URL(fileUrl));
        return Map.entry(fileIdToName.get(fileId), byteArray);
    }

    public ReplyKeyboard replyMarkupForChatSelection(BotIntegrationDataEntity botIntegrationData, Long resendMessageId) {
        List<ForwardOrderProjection> userForwardOrders = forwardOrderRepository
            .getCustomerOrdersByBotIntegrationDataId(botIntegrationData.getId());

        List<InlineKeyboardRow> rows = userForwardOrders.stream()
            .sorted(Comparator.comparing(ForwardOrderProjection::getTechNumber))
            .map(projection -> createSelectChatKeyboardRow(projection, resendMessageId))
            .toList();

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    private InlineKeyboardRow createSelectChatKeyboardRow(ForwardOrderProjection projection, Long messageId) {
        InlineKeyboardButton selectChat = InlineKeyboardButton.builder()
            .text("Чат с автором. Заказ " + projection.getTechNumber())
            .callbackData("/q-send-message-to " + projection.getChatId() + " " + messageId)
            .build();
        InlineKeyboardButton selectAdminChat = InlineKeyboardButton.builder()
            .text("Администрация. Заказ " + projection.getTechNumber())
            .callbackData("/q-send-message-to " + projection.getAdminChatId() + " " + messageId)
            .build();
        return new InlineKeyboardRow(selectChat, selectAdminChat);
    }
}
