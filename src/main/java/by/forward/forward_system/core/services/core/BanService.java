package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.UserDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.AiLogEntity;
import by.forward.forward_system.core.jpa.model.AiViolationEntity;
import by.forward.forward_system.core.jpa.model.SecurityBlockEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.AiLogRepository;
import by.forward.forward_system.core.jpa.repository.AiViolationRepository;
import by.forward.forward_system.core.jpa.repository.SecurityBlockRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.BanProjectionDto;
import by.forward.forward_system.core.services.messager.BotNotificationService;
import by.forward.forward_system.core.services.messager.MessageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BanService {

    private final MessageService messageService;

    private final SecurityBlockRepository securityBlockRepository;

    private final UserService userService;
    private final AiLogRepository aiLogRepository;
    private final AiViolationRepository aiViolationRepository;

    private UserRepository userRepository;

    private BotNotificationService botNotificationService;

    @Transactional
    public boolean ban(Long userId, String reason, List<Long> aiLogId) {
        return ban(userId, reason, false, aiLogId);
    }

    @Transactional
    public boolean ban(Long userId, String reason) {
        return ban(userId, reason, false, Collections.emptyList());
    }

    @Transactional
    public boolean ban(Long userId, String reason, boolean instantBan, List<Long> aiLogId) {
        Optional<SecurityBlockEntity> byId = securityBlockRepository.findByUserId(userId);

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userEntity.getAuthorities().contains(Authority.OWNER)) {
            return false;
        }

        List<AiLogEntity> allAiLogs = aiLogRepository.findAllById(aiLogId);
        LocalDateTime now = LocalDateTime.now();
        for (AiLogEntity aiLog : allAiLogs) {
            AiViolationEntity aiViolationEntity = new AiViolationEntity();

            aiViolationEntity.setUser(userEntity);
            aiViolationEntity.setAiIntegration(aiLog);
            aiViolationEntity.setCreatedAt(now);
            aiViolationEntity.setOldViolation(false);

            aiViolationRepository.save(aiViolationEntity);

            sendViolationNotificationToAdmins(userEntity);
        }

        boolean manyViolations = aiViolationRepository.violationsCount(userId, now.minusMinutes(60)) >= 3;

        if (instantBan || manyViolations) {

            userEntity.addRole(Authority.BANNED);
            userRepository.save(userEntity);

            if (byId.isEmpty()) {
                SecurityBlockEntity securityBlockEntity = new SecurityBlockEntity();
                securityBlockEntity.setIsPermanentBlock(false);
                securityBlockEntity.setCreatedAt(LocalDateTime.now());
                securityBlockEntity.setUser(userEntity);
                securityBlockEntity.setReason(reason);
                securityBlockRepository.save(securityBlockEntity);

                sendNotificationToAdmins(userEntity);
            }

            return true;
        }

        return false;
    }

    public boolean isBanned(Long userId) {
        Optional<SecurityBlockEntity> byId = securityBlockRepository.findByUserId(userId);
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        boolean isUserBanned = userEntity.isPresent() && userEntity.get().getAuthorities().contains(Authority.BANNED);
        return byId.isPresent() || isUserBanned;
    }

    @Transactional
    public void unban(Long userId) {
        Optional<SecurityBlockEntity> byId = securityBlockRepository.findByUserId(userId);
        if (byId.isPresent()) {
            SecurityBlockEntity securityBlockEntity = byId.get();

            UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            userEntity.removeRole(Authority.BANNED);

            List<AiViolationEntity> entities = aiViolationRepository.searchByUserId(userId);
            for (AiViolationEntity violationEntity : entities) {
                violationEntity.setOldViolation(true);
            }
            aiViolationRepository.saveAll(entities);

            userRepository.save(userEntity);
            securityBlockRepository.delete(securityBlockEntity);
        }
    }

    public Integer countNewBaned() {
        return securityBlockRepository.countAllByIsPermanentBlockFalse();
    }

    public List<BanProjectionDto> getAllNewBanned() {
        List<SecurityBlockEntity> allByIsPermanentBlockFalse = securityBlockRepository.findAllByIsPermanentBlockFalse();
        return allByIsPermanentBlockFalse.stream().map(this::toDto).toList();
    }


    public List<BanProjectionDto> getAllBanned() {
        List<SecurityBlockEntity> allByIsPermanentBlockFalse = securityBlockRepository.findAllByIsPermanentBlockTrue();
        return allByIsPermanentBlockFalse.stream().map(this::toDto).toList();
    }

    public BanProjectionDto getBanned(Long banId) {
        SecurityBlockEntity securityBlockEntity = securityBlockRepository.findById(banId).orElseThrow(() -> new RuntimeException("SpamBlockEntity not found"));
        return toDto(securityBlockEntity);
    }

    public BanProjectionDto getBannedByUserId(Long userId) {
        SecurityBlockEntity securityBlockEntity = securityBlockRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("SpamBlockEntity not found"));
        return toDto(securityBlockEntity);
    }

    public BanProjectionDto toDto(SecurityBlockEntity securityBlockEntity) {
        UserDto user = userService.getConverted(securityBlockEntity.getUser().getId());

        List<MessageDto> messagesFromUser = messageService.getMessagesFromUser(user.getId());

        MessageDto messageDto = null;
        if (securityBlockEntity.getChatMessage() != null) {
            messageDto = messageService.getMessageById(securityBlockEntity.getChatMessage().getId());
        }

        return new BanProjectionDto(
            securityBlockEntity.getId(),
            user,
            securityBlockEntity.getReason(),
            messageDto,
            messagesFromUser
        );
    }

    public void setPermanentBan(Long banId) {
        SecurityBlockEntity securityBlockEntity = securityBlockRepository.findById(banId).orElseThrow(() -> new RuntimeException("SpamBlockEntity not found"));
        securityBlockEntity.setIsPermanentBlock(true);
        securityBlockRepository.save(securityBlockEntity);
    }

    public List<UserDto> getAllUsersForBan() {
        return userService.getAllUserWithoutRole(Authority.OWNER).stream()
            .filter(u -> !u.getAuthorities().contains(Authority.BANNED))
            .map(userService::convertUserToDto)
            .sorted(Comparator.comparing(a -> a.getUsername().toLowerCase()))
            .toList();
    }

    private void sendViolationNotificationToAdmins(UserEntity userEntity) {
        String text = """
            Сообщение для %s.
            Пользователь %s нарушил правила общения.
            """;
        List<UserEntity> allUserWithoutRole = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        for (UserEntity admin : allUserWithoutRole) {
            botNotificationService.sendBotNotification(admin.getId(), text.formatted(admin.getUsername(), userEntity.getUsername()));
        }
    }

    private void sendNotificationToAdmins(UserEntity userEntity) {
        String text = """
            СРОЧНО!
            
            Срочное сообщение для %s.
            Система заблокировала пользоавтеля: %s.
            Проверьте правильность блокировки пользователя.
            
            СРОЧНО!
            """;
        List<UserEntity> allUserWithoutRole = userService.findUsersWithRole(Authority.ADMIN.getAuthority());
        for (UserEntity admin : allUserWithoutRole) {
            botNotificationService.sendBotNotification(admin.getId(), text.formatted(admin.getUsername(), userEntity.getUsername()));
        }
    }
}
