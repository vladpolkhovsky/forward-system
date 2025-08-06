package by.forward.forward_system.core.web.rest;

import by.forward.forward_system.core.dto.rest.authors.AuthorDisciplinesDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorDto;
import by.forward.forward_system.core.dto.rest.authors.AuthorOrderDto;
import by.forward.forward_system.core.services.NewAuthorService;
import by.forward.forward_system.core.services.NewOrderService;
import by.forward.forward_system.core.utils.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/author")
@RequiredArgsConstructor
public class AuthorRestController {

    private final NewOrderService newOrderService;
    private final NewAuthorService newAuthorService;

    @GetMapping(value = "/get-author-orders")
    public ResponseEntity<List<AuthorOrderDto>> getAuthorOrders() {
        var orders = newOrderService.getAuthorOrders(AuthUtils.getCurrentUserId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/get-author-orders/{authorId}")
    public ResponseEntity<List<AuthorOrderDto>> getAuthorOrdersById(@PathVariable Long authorId) {
        var orders = newOrderService.getAuthorOrders(authorId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/get-author-disciplines/{userId}")
    public ResponseEntity<AuthorDisciplinesDto> getAuthorDiscipline(@PathVariable Long userId) {
        var disciplines = newAuthorService.getAuthorDisciplines(userId);
        return ResponseEntity.ok(disciplines);
    }

    @GetMapping(value = "/get-authors")
    public ResponseEntity<List<AuthorDto>> getAuthors() {
        var authors = newAuthorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }
}
