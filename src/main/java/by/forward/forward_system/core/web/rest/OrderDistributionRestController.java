package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.v3.CreateDistributionRequestDto;
import by.forward.forward_system.core.dto.messenger.v3.DistributionLogDto;
import by.forward.forward_system.core.enums.DistributionItemStatusType;
import by.forward.forward_system.core.enums.ParticipantType;
import by.forward.forward_system.core.jpa.model.QueueDistributionItemEntity;
import by.forward.forward_system.core.jpa.repository.ChatMessageOptionRepository;
import by.forward.forward_system.core.jpa.repository.QueueDistributionItemRepository;
import by.forward.forward_system.core.services.NewDistributionService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/order/distribution")
@RequiredArgsConstructor
public class OrderDistributionRestController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";
    private final NewDistributionService newDistributionService;
    private final QueueDistributionItemRepository queueDistributionItemRepository;
    private final ChatMessageOptionRepository chatMessageOptionRepository;

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopDistribution(@RequestParam("distributionId") Long distributionId) {
        newDistributionService.stopDistribution(distributionId);
        return ResponseEntity.ok(Map.of("code", 200,
            "distributionId", distributionId));
    }

    @PostMapping(value = "/delete-author-participant", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Object>> deleteAuthorParticipant(@RequestParam("orderId") Long orderId,
                                                                       @RequestParam("authorId") Long authorId) {
        newDistributionService.deleteAuthorParticipant(orderId, authorId);
        return ResponseEntity.ok(Map.of("code", 200,
            "orderId", orderId,
            "authorId", authorId));
    }

    @PostMapping(value = "/set/catcher", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Object>> setCatcher(@RequestParam("orderId") Long orderId,
                                                          @RequestParam("catcherId") Long catcherId) {
        newDistributionService.updateManagerRole(orderId, catcherId, ParticipantType.CATCHER);
        return ResponseEntity.ok(Map.of("code", 200,
            "orderId", orderId,
            "catcherId", catcherId));
    }

    @PostMapping(consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Object>> createDistribution(@RequestParam("orderId") Long orderId,
                                                                  @RequestBody CreateDistributionRequestDto request) {
        newDistributionService.createDistribution(orderId, request);
        return ResponseEntity.ok(Map.of("code", 200,
            "orderId", orderId));
    }

    @GetMapping(consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<List<DistributionLogDto>> list(@RequestParam("orderId") Long orderId) {
        return ResponseEntity.ok(newDistributionService.listLogs(orderId));
    }

    @PostMapping(value = "/skip-item", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    @Transactional
    public ResponseEntity<Map<String, Object>> skipItem(@RequestParam("itemId") Long itemId) {
        QueueDistributionItemEntity item = queueDistributionItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Не найден элемент распределения заказа с id = " + itemId));
        newDistributionService.skip(item);
        return ResponseEntity.ok(Map.of("code", 200,
            "itemId", itemId));
    }

    @GetMapping("/item")
    @Transactional
    public ResponseEntity<Map<String, Object>> processUserAction(@RequestParam("itemId") Long itemId,
                                                                 @RequestParam("optionId") Long optionId,
                                                                 @RequestParam("action") DistributionItemStatusType action) {
        if (!List.of(DistributionItemStatusType.DECLINE, DistributionItemStatusType.TALK, DistributionItemStatusType.ACCEPTED).contains(action)) {
            throw new IllegalArgumentException("Некорректный тип взаимодействия action = " + action);
        }

        QueueDistributionItemEntity item = queueDistributionItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Не найден элемент распределения заказа с id = " + itemId));

        chatMessageOptionRepository.setOptionResolved(optionId);

        if (!Objects.equals(item.getUser().getId(), AuthUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("Вы не можете выполнить данное действие.");
        }

        if (action == DistributionItemStatusType.DECLINE) {
            newDistributionService.decline(item);
        }
        if (action == DistributionItemStatusType.TALK) {
            newDistributionService.talk(item);
        }
        if (action == DistributionItemStatusType.ACCEPTED) {
            newDistributionService.accept(item);
        }

        return ResponseEntity.ok(Map.of("itemId", itemId,
            "optionId", optionId,
            "action", action,
            "code", 200
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> distributionExceptionHandler(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", ex.getMessage(),
                "code", 400,
                "stack", ExceptionUtils.getStackTrace(ex)));
    }
}
