package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.dto.messenger.MessageDto;
import by.forward.forward_system.core.dto.messenger.UserDto;
import by.forward.forward_system.core.dto.ui.UserUiDto;
import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.SpamBlockEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.SpamBlockRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import by.forward.forward_system.core.jpa.repository.projections.BanProjectionDto;
import by.forward.forward_system.core.services.messager.MessageService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BanService {

    private final MessageService messageService;
    private final SpamBlockRepository spamBlockRepository;
    private final UserService userService;

    private UserRepository userRepository;

    public boolean ban(Long userId) {
        Optional<SpamBlockEntity> byId = spamBlockRepository.findByUserId(userId);

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userEntity.getAuthorities().contains(Authority.OWNER)) {
            return false;
        }

        userEntity.addRole(Authority.BANNED);
        userRepository.save(userEntity);

        if (byId.isEmpty()) {
            SpamBlockEntity spamBlockEntity = new SpamBlockEntity();
            spamBlockEntity.setIsPermanentBlock(false);
            spamBlockEntity.setCreatedAt(LocalDateTime.now());
            spamBlockEntity.setUser(userEntity);
            spamBlockRepository.save(spamBlockEntity);
        }

        return true;
    }

    public boolean isBanned(Long userId) {
        Optional<SpamBlockEntity> byId = spamBlockRepository.findByUserId(userId);
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        boolean isUserBanned = userEntity.isPresent() && userEntity.get().getAuthorities().contains(Authority.BANNED);
        return byId.isPresent() || isUserBanned;
    }

    public void unban(Long userId) {
        Optional<SpamBlockEntity> byId = spamBlockRepository.findById(userId);
        if (byId.isPresent()) {
            SpamBlockEntity spamBlockEntity = byId.get();

            UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            userEntity.removeRole(Authority.BANNED);

            userRepository.save(userEntity);
            spamBlockRepository.delete(spamBlockEntity);
        }
    }

    public Integer countNewBaned() {
        return spamBlockRepository.countAllByIsPermanentBlockFalse();
    }

    public List<BanProjectionDto> getAllNewBanned() {
        List<SpamBlockEntity> allByIsPermanentBlockFalse = spamBlockRepository.findAllByIsPermanentBlockFalse();
        return allByIsPermanentBlockFalse.stream().map(this::toDto).toList();
    }

    public BanProjectionDto getBanned(Long banId) {
        SpamBlockEntity spamBlockEntity = spamBlockRepository.findById(banId).orElseThrow(() -> new RuntimeException("SpamBlockEntity not found"));
        return toDto(spamBlockEntity);
    }

    public BanProjectionDto toDto(SpamBlockEntity spamBlockEntity) {
        UserDto user = userService.getConverted(spamBlockEntity.getId());
        List<MessageDto> messagesFromUser = messageService.getMessagesFromUser(user.getId());
        return new BanProjectionDto(spamBlockEntity.getId(), user, messagesFromUser);
    }
}
