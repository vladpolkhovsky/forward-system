package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.enums.auth.Authority;
import by.forward.forward_system.core.jpa.model.SpamBlockEntity;
import by.forward.forward_system.core.jpa.model.UserEntity;
import by.forward.forward_system.core.jpa.repository.SpamBlockRepository;
import by.forward.forward_system.core.jpa.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BanService {

    private SpamBlockRepository spamBlockRepository;

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
        return spamBlockRepository.findAllByIsPermanentBlockFalse();
    }
}
