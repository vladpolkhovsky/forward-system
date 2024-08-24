package by.forward.forward_system.core.services.messager;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SpamDetectorService {

    private final int MESSAGE_LIMIT = 10;

    private final int PERIOD_MIN = 1;

    private final Map<Long, List<LocalDateTime>> messagesTime = new HashMap<>(256);

    public boolean isSpam(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        refreshMap(now.minusMinutes(PERIOD_MIN));
        saveMessage(userId, now);
        if (messagesTime.get(userId).size() >= MESSAGE_LIMIT) {
            return true;
        }
        return false;
    }

    private void saveMessage(Long userId, LocalDateTime now) {
        List<LocalDateTime> messages = messagesTime.getOrDefault(userId, new ArrayList<>(10));
        messages.add(now);
        messagesTime.put(userId, messages);
    }

    private void refreshMap(LocalDateTime limit) {
        List<Long> toRemove = new ArrayList<>();
        for (Map.Entry<Long, List<LocalDateTime>> userMessages : messagesTime.entrySet()) {
            List<LocalDateTime> messages = userMessages.getValue();
            messages.removeIf(t -> t.isBefore(limit));
            if (messages.isEmpty()) {
                toRemove.add(userMessages.getKey());
            }
        }
        for (Long id : toRemove) {
            messagesTime.remove(id);
        }
    }
}
