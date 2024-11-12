package by.forward.forward_system.core.jpa.repository;

import by.forward.forward_system.core.enums.BotType;
import by.forward.forward_system.core.jpa.model.BotIntegrationDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotIntegrationDataRepository extends JpaRepository<BotIntegrationDataEntity, Long> {

    @Query(nativeQuery = true, value = "select * from forward_system.bot_integration_data bid where bid.user_id = :userId and bid.bot_type = :botType and bid.telegram_chat_id = :telegramChatId and bid.telegram_user_id = :telegramUserId")
    Optional<BotIntegrationDataEntity> getIntegrationDataByFullInfo(Long userId, String botType, Long telegramChatId, Long telegramUserId);

    @Query(nativeQuery = true, value = "select * from forward_system.bot_integration_data bid where bid.user_id = :userId")
    List<BotIntegrationDataEntity> getBotIntegrationDataByUserId(Long userId);

    @Query(nativeQuery = true, value = "select * from forward_system.bot_integration_data bid where bid.telegram_chat_id = :chatId;")
    List<BotIntegrationDataEntity> getBotIntegrationDataByChatId(Long chatId);

    @Query(nativeQuery = true, value = "select bid.user_id from forward_system.bot_integration_data bid where bid.telegram_chat_id = :chatId")
    Optional<Long> findUserIdByChatId(Long chatId);
}
