package by.forward.forward_system.core.services.core;

import by.forward.forward_system.core.jpa.model.UserActivityEntity;
import by.forward.forward_system.core.jpa.repository.UserActivityRepository;
import by.forward.forward_system.core.jpa.repository.projections.UserActivityDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    @Transactional
    public void updateUserActivity(Long userId) {
        Optional<UserActivityEntity> byId = userActivityRepository.findById(userId);
        byId.ifPresent(userActivityEntity -> userActivityRepository.updateLastOnlineDate(userId));

        if (byId.isEmpty()) {
            userActivityRepository.save(new UserActivityEntity(userId, LocalDateTime.now()));
        }
    }

    public List<UserActivityDto> getAllUserActivity() {
        return userActivityRepository.loadAllUserActivity();
    }
}
