package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.messenger.v3.V3SearchOrderReviewDto;
import by.forward.forward_system.core.services.NewReviewService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
