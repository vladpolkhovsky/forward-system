package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.*;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.repository.ReviewRepository;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.ReviewService;
import by.forward.forward_system.core.services.ui.AuthorUiService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class ReviewController {

    private final UserUiService userUiService;
    private final AuthorUiService authorUiService;
    private final OrderUiService orderUiService;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final OrderService orderService;
    private final AttachmentService attachmentService;

    @GetMapping(value = "/review-order")
    public String reviewSelector(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.FINALIZATION.getName())
        );

        allOrdersInStatus = allOrdersInStatus.stream()
            .sorted(Comparator.comparing(OrderUiDto::getId).reversed())
            .toList();

        model.addAttribute("menuName", "Выберите заказ, который хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orders", allOrdersInStatus);

        return "main/expert-review-order-selector";
    }

    @GetMapping(value = "/edit-review-order")
    public String editReviewSelector(Model model) {
        List<ReviewProjectionDto> notReviewed = reviewService.getNotReviewed();

        model.addAttribute("menuName", "Выберите запрос, который хотите редактировать и отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", notReviewed);

        return "main/expert-review-edit-order-selector";
    }

    @GetMapping(value = "/expert-review-order/{orderId}")
    public String expertReviewOrder(Model model, @PathVariable Long orderId) {
        List<ChatAttachmentProjectionDto> chatAttachments = orderUiService.getOrderMainChatAttachments(orderId).stream()
            .map(t -> new ChatAttachmentProjectionDto(
                t.getFirstname() + " " + t.getLastname().substring(0, 1),
                t.getAttachmentFileId(),
                t.getAttachmentFilename(),
                t.getAttachmentTime(),
                false))
            .toList();

        List<AuthorUiDto> authors = authorUiService.getAllAuthors().stream().filter(AuthorUiDto::getIsAuthor).toList();
        List<UserSelectionUiDto> selection = authors.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), false))
            .sorted(Comparator.comparing(UserSelectionUiDto::getUsername))
            .toList();

        Optional<UserSelectionUiDto> first = selection.stream().findFirst();
        if (first.isPresent()) {
            first.get().setChecked(true);
        }

        model.addAttribute("experts", selection);

        model.addAttribute("menuName", "Выберите сообщение, которое хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orderId", orderId);
        model.addAttribute("files", chatAttachments);

        return "main/expert-review-order";
    }

    @GetMapping(value = "/expert-review-requests")
    public String expertReviewRequests(Model model) {
        List<ReviewProjectionDto> notReviewedByUser = reviewService.getNotReviewedByUser(userUiService.getCurrentUserId());

        model.addAttribute("menuName", "Выберите заказ, который хотите проверить");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", notReviewedByUser);

        return "main/expert-review-requests";
    }

    @GetMapping(value = "/expert-review-order-answer/{orderId}/{reviewId}")
    public String expertAnswer(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        ReviewDto reviewById = reviewService.getReviewById(reviewId);

        model.addAttribute("menuName", "Проверьте работу и напишите вердикт.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("review", reviewById);

        return "main/expert-review-order-answer";
    }

    @SneakyThrows
    @PostMapping(value = "/expert-review-order-answer/{orderId}/{reviewId}")
    public RedirectView expertAnswer(@PathVariable Long orderId,
                                     @PathVariable Long reviewId,
                                     @RequestParam("verdict") String verdict,
                                     @RequestParam("verdict-mark") String verdictMark,
                                     @RequestParam("verdict-file") MultipartFile file) {

        AttachmentEntity attachmentEntity = attachmentService.saveAttachment(file.getOriginalFilename(), file.getBytes());

        reviewService.saveVerdict(reviewId, verdict, verdictMark, attachmentEntity.getId());
        orderService.changeStatus(orderId, OrderStatus.FINALIZATION);

        return new RedirectView("/main");
    }

    @GetMapping(value = "/edit-review-order/{orderId}/{reviewId}")
    public String expertReviewOrder(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Not fount"));

        List<ChatAttachmentProjectionDto> chatAttachments = orderUiService.getOrderMainChatAttachments(orderId).stream()
            .map(t -> new ChatAttachmentProjectionDto(
                t.getFirstname() + " " + t.getLastname().substring(0, 1),
                t.getAttachmentFileId(),
                t.getAttachmentFilename(),
                t.getAttachmentTime(),
                t.getAttachmentFileId().equals(reviewEntity.getAttachment().getId())
            ))
            .toList();

        List<AuthorUiDto> authors = authorUiService.getAllAuthors().stream().filter(AuthorUiDto::getIsAuthor).toList();
        List<UserSelectionUiDto> selection = authors.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), reviewEntity.getReviewedBy().getId().equals(t.getId())))
            .sorted(Comparator.comparing(UserSelectionUiDto::getUsername))
            .toList();

        model.addAttribute("experts", selection);

        model.addAttribute("menuName", "Выберите сообщение, которое хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orderId", orderId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("messageText", reviewEntity.getReviewMessage());
        model.addAttribute("files", chatAttachments);

        return "main/expert-review-order";
    }

    @GetMapping(value = "/review-order-answers")
    public String reviewOrderAnswers(Model model) {
        List<ReviewProjectionDto> all = reviewService.getAllReviews();

        model.addAttribute("menuName", "Выберите заказ, который хотите посмотреть");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", all);

        return "main/expert-review-view-selector";
    }

    @GetMapping(value = "/expert-review-order-view/{orderId}/{reviewId}")
    public String expertReviewOrderView(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        ReviewDto reviewById = reviewService.getReviewById(reviewId);

        model.addAttribute("menuName", "Проверьте работу и напишите вердикт.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("review", reviewById);

        return "main/expert-review-order-view";
    }

    @PostMapping(value = "/expert-review-order/{orderId}")
    public RedirectView expertReviewOrder(@PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        String reviewId = body.getFirst("reviewId");
        Long expertId = Long.parseLong(body.getFirst("expertId"));

        Long attachmentId = Long.parseLong(body.getFirst("attachmentId"));
        String messageText = body.getFirst("reviewText");

        if (reviewId == null) {
            reviewService.saveNewReviewRequest(orderId, expertId, attachmentId, messageText);
        } else {
            reviewService.updateReviewRequest(orderId, expertId, Long.parseLong(reviewId), attachmentId, messageText);
        }

        orderService.changeStatus(orderId, OrderStatus.REVIEW);

        return new RedirectView("/main");
    }

    @GetMapping(value = "/review-order-answers-accept/{orderId}/{reviewId}")
    public RedirectView reviewOrderAnswers(@PathVariable Long orderId,
                                           @PathVariable Long reviewId,
                                           @RequestParam(value = "verdict") Boolean verdict,
                                           @RequestParam(value = "send-to-chat", defaultValue = "false") Boolean sendToChat) {
        reviewService.acceptReview(orderId, reviewId, verdict, sendToChat);
        return new RedirectView("/main");
    }

}
