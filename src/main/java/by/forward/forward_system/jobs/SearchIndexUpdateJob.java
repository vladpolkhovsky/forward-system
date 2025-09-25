package by.forward.forward_system.jobs;

import by.forward.forward_system.core.jpa.model.ChatEntity_;
import by.forward.forward_system.core.jpa.model.ChatSearchIndexUpdateEntity;
import by.forward.forward_system.core.jpa.model.ChatTagSearchIndexUpdateEntity;
import by.forward.forward_system.core.jpa.repository.ChatSearchIndexUpdateRepository;
import by.forward.forward_system.core.jpa.repository.ChatTagSearchIndexUpdateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
public class SearchIndexUpdateJob {

    private static final long PAGE_SIZE = 500;

    private final IndexUpdater indexUpdater;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        new Thread(() -> updateChatIndexJob()).start();
        new Thread(() -> updateTagIndexJob()).start();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void updateChatIndexJob() {
        log.info("Страт индексации имён чата");
        Pageable pageable = PageRequest.of(0, (int) PAGE_SIZE, Sort.by(ChatEntity_.ID));
        while (indexUpdater.updateChatPageIndexes(pageable)) {
            log.info("[Индексации имён чата] Страница {} обработана.", pageable);
            pageable = pageable.next();
        }
        log.info("Финиш индексации имён чата");
    }

    @Scheduled(cron = "0 0 */12 * * *")
    public void updateTagIndexJob() {
        log.info("Страт индексации имён тегов");
        Pageable pageable = PageRequest.of(0, (int) PAGE_SIZE, Sort.by(ChatEntity_.ID));
        while (indexUpdater.updateChatTagPageIndexes(pageable)) {
            log.info("[Индексация имён тегов] Страница {} обработана.", pageable);
            pageable = pageable.next();
        }
        log.info("Финиш индексации имён тегов");

    }

    @Component
    @AllArgsConstructor
    public static class IndexUpdater {

        private final ChatSearchIndexUpdateRepository chatIndexRepository;
        private final ChatTagSearchIndexUpdateRepository chatTagIndexRepository;

        @Transactional
        public boolean updateChatPageIndexes(Pageable pageable) {
            Page<ChatSearchIndexUpdateEntity> page = chatIndexRepository.findAll(pageable);

            page.forEach(chat -> {
                String chatName = Optional.ofNullable(chat.getChatName())
                    .map(String::toLowerCase)
                    .orElse("");

                List<String> lexemes = toLexemes(chatName);
                chat.setTokens(String.join(" ", lexemes));
            });

            return page.hasNext();
        }

        @Transactional
        public boolean updateChatTagPageIndexes(Pageable pageable) {
            Page<ChatTagSearchIndexUpdateEntity> page = chatTagIndexRepository.findAll(pageable);

            page.forEach(chat -> {
                String chatName = Optional.ofNullable(chat.getName())
                    .map(String::toLowerCase)
                    .orElse("");

                List<String> lexemes = toLexemes(chatName);
                chat.setTokens(String.join(" ", lexemes));
            });

            return page.hasNext();
        }

        private static List<String> toLexemes(String line) {
            List<String> allLexemes = Arrays.stream(line.split("[^a-zA-zА-Яа-я0-9]+"))
                .filter(StringUtils::isNotBlank)
                .toList();

            List<String> numbers = allLexemes.stream().filter(StringUtils::isNumeric).toList();
            List<String> others = allLexemes.stream().filter(Predicate.not(StringUtils::isNumeric))
                .flatMap(lexeme -> {
                    if (lexeme.length() <= 3) {
                        return Stream.of(lexeme);
                    }
                    return IntStream.range(3, lexeme.length() + 1)
                        .mapToObj(end -> lexeme.substring(0, end));
                })
                .toList();

            return Stream.concat(numbers.stream(), others.stream()).distinct().toList();
        }
    }
}
