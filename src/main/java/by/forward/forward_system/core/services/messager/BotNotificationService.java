package by.forward.forward_system.core.services.messager;

import by.forward.forward_system.core.enums.BotType;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.*;
import by.forward.forward_system.core.jpa.repository.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class BotNotificationService {

    private final UserRepository userRepository;

    private final BotNotificationCodeRepository botNotificationCodeRepository;

    private final BotIntegrationDataRepository botIntegrationDataRepository;

    private final BotTypeRepository botTypeRepository;

    private final WebPushNotification webPushNotification;

    private final TelegramClient telegramClient;

    private final ForwardOrderRepository forwardOrderRepository;

    private final CustomerTelegramToForwardOrderRepository customerTelegramToForwardOrderRepository;

    @Transactional
    public String getCode(Long currentUserId) {
        Optional<BotNotificationCodeEntity> byId = botNotificationCodeRepository.findById(currentUserId);

        if (byId.isEmpty()) {
            UserEntity userEntity = userRepository.findById(currentUserId).orElseThrow();

            boolean codeExists = true;
            long newCode = 0L;

            while (codeExists) {
                newCode = 100_000 + (new Random().nextLong(100_000L, 800_000L));
                codeExists = botNotificationCodeRepository.existsByCode(Long.toString(newCode));
            }

            BotNotificationCodeEntity botNotificationCodeEntity = new BotNotificationCodeEntity();
            botNotificationCodeEntity.setCode(Long.toString(newCode));
            botNotificationCodeEntity.setUser(userEntity);
            botNotificationCodeRepository.save(botNotificationCodeEntity);
        }

        BotNotificationCodeEntity botNotificationCodeEntity = botNotificationCodeRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("Not found BotNotificationCodeEntity"));

        return botNotificationCodeEntity.getCode();
    }

    @Transactional
    public boolean registerBotUser(Long telegramChatId, Long telegramUserId, String code) {
        Optional<BotNotificationCodeEntity> byCode = botNotificationCodeRepository.findByCode(code);
        if (byCode.isEmpty()) {
            return false;
        }

        UserEntity user = byCode.get().getUser();

        Long userId = user.getId();
        Optional<BotIntegrationDataEntity> integrationDataByFullInfo = botIntegrationDataRepository.getIntegrationDataByFullInfo(userId, BotType.TELEGRAM_BOT.getName(), telegramChatId, telegramUserId);

        if (integrationDataByFullInfo.isEmpty()) {
            BotTypeEntity botTypeEntity = botTypeRepository.findById(BotType.TELEGRAM_BOT.getName()).get();

            BotIntegrationDataEntity botIntegrationDataEntity = new BotIntegrationDataEntity();
            botIntegrationDataEntity.setBotType(botTypeEntity);
            botIntegrationDataEntity.setTelegramChatId(telegramChatId);
            botIntegrationDataEntity.setTelegramUserId(telegramUserId);
            botIntegrationDataEntity.setUser(user);

            botIntegrationDataRepository.save(botIntegrationDataEntity);
        }

        return true;
    }

    @Transactional
    @SneakyThrows
    public Optional<ForwardOrderEntity> registerBotUserByForwardOrder(Long telegramChatId, Long telegramUserId, String code) {
        Optional<ForwardOrderEntity> byCode = forwardOrderRepository.findByCode(code);

        if (byCode.isPresent()) {
            BotIntegrationDataEntity botIntegrationData = null;

            Optional<BotIntegrationDataEntity> firstByTelegramChatId = botIntegrationDataRepository
                .findFirstByTelegramChatId(telegramChatId, BotType.CUSTOMER_TELEGRAM_BOT.getName());

            if (firstByTelegramChatId.isEmpty()) {
                BotTypeEntity botTypeEntity = botTypeRepository.findById(BotType.CUSTOMER_TELEGRAM_BOT.getName())
                    .get();

                BotIntegrationDataEntity botIntegrationDataEntity = new BotIntegrationDataEntity();
                botIntegrationDataEntity.setBotType(botTypeEntity);
                botIntegrationDataEntity.setTelegramChatId(telegramChatId);
                botIntegrationDataEntity.setTelegramUserId(telegramUserId);

                botIntegrationData = botIntegrationDataRepository.save(botIntegrationDataEntity);
            } else {
                botIntegrationData = firstByTelegramChatId.get();
            }

            CustomerTelegramToForwardOrderEntity customerTelegramToForwardOrder = new CustomerTelegramToForwardOrderEntity();
            customerTelegramToForwardOrder.setForwardOrder(byCode.get());
            customerTelegramToForwardOrder.setBotIntegrationData(botIntegrationData);
            customerTelegramToForwardOrder.setLastUpdateAt(LocalDateTime.now().minusYears(1));
            customerTelegramToForwardOrder.setCreatedAt(LocalDateTime.now());

            customerTelegramToForwardOrderRepository.save(customerTelegramToForwardOrder);
        }

        return byCode;
    }

    @Transactional
    public void unsubscribe(Long chatId) {
        List<BotIntegrationDataEntity> botIntegrationDataByChatId = botIntegrationDataRepository.getBotIntegrationDataByChatId(chatId);
        botIntegrationDataRepository.deleteAll(botIntegrationDataByChatId);
    }

    @Transactional
    public boolean sendBotNotification(Long userId, String messageText) {
        webPushNotification.sendNotification(userId, "У вас есть непрочитанные сообщения.", messageText);
        List<BotIntegrationDataEntity> botIntegrationDataByUserId = botIntegrationDataRepository.getBotIntegrationDataByUserId(userId);
        for (BotIntegrationDataEntity botIntegrationDataEntity : botIntegrationDataByUserId) {
            try {
                telegramClient.execute(SendMessage.builder()
                    .chatId(botIntegrationDataEntity.getTelegramChatId())
                    .text(messageText)
                    .build());
            } catch (TelegramApiException ignored) {

            }
        }
        return true;
    }

    @Transactional
    public void sendBotNotificationToAdmins(String messageText) {
        List<Long> adminIds = userRepository.findUserIdsWithRole(Authority.ADMIN.getAuthority());
        List<BotIntegrationDataEntity> botIntegrationDataEntities = botIntegrationDataRepository.getBotIntegrationDataByUserIdIn(adminIds);
        for (BotIntegrationDataEntity botIntegrationDataEntity : botIntegrationDataEntities) {
            try {
                telegramClient.execute(SendMessage.builder()
                    .chatId(botIntegrationDataEntity.getTelegramChatId())
                    .text(messageText)
                    .build());
            } catch (TelegramApiException ignored) {

            }
        }
    }

    public Optional<Long> getUserIdByChatId(Long chatId) {
        return botIntegrationDataRepository.findUserIdByChatId(chatId);
    }
}
