package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.v3.V3ReviewCreateRequest;
import by.forward.forward_system.core.dto.messenger.v3.V3SearchOrderReviewDto;
import by.forward.forward_system.core.services.NewReviewService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/review")
@AllArgsConstructor
public class ReviewRestController {

    private final static String JSON_MEDIA_TYPE = "application/json; charset=UTF-8";

    private final NewReviewService newReviewService;

    @GetMapping(value = "/search", produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Page<V3SearchOrderReviewDto>> getSearch(@RequestParam("techNumber") String techNumber,
                                                                  @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(newReviewService.search(techNumber, pageable));
    }

    @PostMapping(value = "/create", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody V3ReviewCreateRequest createRequest) {
        newReviewService.createAutomaticReviewRequest(createRequest);
        return ResponseEntity.ok(Map.of("code", 200));
    }

    @PostMapping(value = "/delete/{reviewId}", consumes = JSON_MEDIA_TYPE, produces = JSON_MEDIA_TYPE)
    public ResponseEntity<Map<String, Object>> createReview(@PathVariable Long reviewId, @RequestParam("chatId") Long chatId) {
        newReviewService.deleteReview(reviewId, chatId);
        return ResponseEntity.ok(Map.of("code", 200));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> distributionExceptionHandler(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", ex.getMessage(),
                "code", 400,
                "stack", ExceptionUtils.getStackTrace(ex)));
    }
}
