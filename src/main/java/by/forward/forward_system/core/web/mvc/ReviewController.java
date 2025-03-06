package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.AuthorUiDto;
import by.forward.forward_system.core.dto.ui.OrderUiDto;
import by.forward.forward_system.core.dto.ui.ReviewDto;
import by.forward.forward_system.core.dto.ui.UserSelectionUiDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.model.AttachmentEntity;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import by.forward.forward_system.core.jpa.repository.ReviewRepository;
import by.forward.forward_system.core.jpa.repository.projections.ChatAttachmentProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.ReviewProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.SimpleOrderProjection;
import by.forward.forward_system.core.services.core.AttachmentService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.ReviewService;
import by.forward.forward_system.core.services.ui.AuthorUiService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
        List<SimpleOrderProjection> allOrdersInStatus = orderUiService.findAllOrdersInStatusProjection(Arrays.asList(
            OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.FINALIZATION.getName())
        );

        model.addAttribute("menuName", "Выберите заказ, который хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orders", allOrdersInStatus);

        return "main/expert-review-order-selector";
    }

    @GetMapping(value = "/edit-review-order")
    public String editReviewSelector(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        int pageCount = reviewService.getNotReviewedPageCount();
        page = Math.max(Math.min(pageCount, page), 1);
        List<Integer> pages = IntStream.range(1, pageCount + 1).boxed().toList();

        List<ReviewProjectionDto> notReviewed = reviewService.getNotReviewed(page);

        model.addAttribute("menuName", "Выберите запрос, который хотите редактировать и отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", notReviewed);
        model.addAttribute("showPages", true);
        model.addAttribute("page", page);
        model.addAttribute("pages", pages);

        return "main/expert-review-edit-order-selector";
    }

    @GetMapping(value = "/expert-review-order/{orderId}")
    public String expertReviewOrder(Model model, @PathVariable Long orderId) {
        userUiService.checkAccessManager();

        OrderUiDto order = orderUiService.getOrder(orderId);

        List<ChatAttachmentProjectionDto> chatAttachments = orderUiService.getOrderMainChatAttachments(orderId).stream()
            .map(t -> new ChatAttachmentProjectionDto(
                t.getFirstname() + " " + t.getLastname().substring(0, 1),
                t.getUsername(),
                t.getAttachmentFileId(),
                t.getAttachmentFilename(),
                t.getAttachmentTime(),
                false))
            .toList();

        List<AuthorUiDto> authors = authorUiService.getAllAuthorsFast().stream().filter(AuthorUiDto::getIsAuthor).toList();
        List<UserSelectionUiDto> selection = authors.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), false))
            .sorted(Comparator.comparing(UserSelectionUiDto::getUsername))
            .toList();

        Optional<UserSelectionUiDto> first = selection.stream().findFirst();
        if (first.isPresent()) {
            first.get().setChecked(true);
        }

        boolean isOkStatus = Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.FINALIZATION)
            .contains(OrderStatus.byName(order.getOrderStatus()));
        String techNumber = order.getTechNumber().toString();

        model.addAttribute("experts", selection);

        model.addAttribute("menuName", "Выберите сообщение, которое хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orderId", orderId);
        model.addAttribute("files", chatAttachments);
        model.addAttribute("isOkStatus", isOkStatus);
        model.addAttribute("techNumber", techNumber);

        return "main/expert-review-order";
    }

    @GetMapping(value = "/expert-review-requests")
    public String expertReviewRequests(Model model) {
        List<ReviewProjectionDto> notReviewedByUser = reviewService.getNotReviewedByUser(userUiService.getCurrentUserId());
        List<ReviewProjectionDto> reviewedByUser = reviewService.getReviewedByUser(userUiService.getCurrentUserId());

        model.addAttribute("menuName", "Выберите заказ, который хотите проверить");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", notReviewedByUser);
        model.addAttribute("oldReviews", reviewedByUser);

        return "main/expert-review-requests";
    }

    @GetMapping(value = "/expert-review-order-answer/{orderId}/{reviewId}")
    public String expertAnswer(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        ReviewDto reviewById = reviewService.getReviewById(reviewId);
        List<ReviewDto> olderReviews = reviewService.getOlderReviews(orderId);

        model.addAttribute("menuName", "Проверьте работу и напишите вердикт.");
        model.addAttribute("olderReviews", olderReviews);
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("review", reviewById);
        model.addAttribute("additionalFile", reviewById.getAdditionalAttachmentId());
        model.addAttribute("orderId", orderId);

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

        orderService.notifyVerdictSaved(orderId);

        orderService.changeStatus(orderId, OrderStatus.FINALIZATION);

        return new RedirectView("/main");
    }

    @GetMapping(value = "/edit-review-order/{orderId}/{reviewId}")
    public String expertReviewOrder(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        OrderUiDto order = orderUiService.getOrder(orderId);

        ReviewEntity reviewEntity = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Not fount"));

        Long additionalFileId = reviewEntity.getAdditionalAttachment() == null ? null : reviewEntity.getAdditionalAttachment().getId();

        List<ChatAttachmentProjectionDto> chatAttachments = orderUiService.getOrderMainChatAttachments(orderId).stream()
            .map(t -> new ChatAttachmentProjectionDto(
                t.getFirstname() + " " + t.getLastname().substring(0, 1),
                t.getUsername(),
                t.getAttachmentFileId(),
                t.getAttachmentFilename(),
                t.getAttachmentTime(),
                t.getAttachmentFileId().equals(reviewEntity.getAttachment().getId())
            ))
            .toList();

        List<AuthorUiDto> authors = authorUiService.getAllAuthorsFast().stream().filter(AuthorUiDto::getIsAuthor).toList();
        List<UserSelectionUiDto> selection = authors.stream()
            .map(t -> new UserSelectionUiDto(t.getId(), t.getFio(), t.getUsername(), reviewEntity.getReviewedBy().getId().equals(t.getId())))
            .sorted(Comparator.comparing(UserSelectionUiDto::getUsername))
            .toList();

        model.addAttribute("experts", selection);

        boolean isOkStatus = Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.FINALIZATION)
            .contains(OrderStatus.byName(order.getOrderStatus()));
        String techNumber = order.getTechNumber().toString();

        model.addAttribute("menuName", "Выберите сообщение, которое хотите отправить на рассмотрение эксперта");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("orderId", orderId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("hasFile", additionalFileId != null);
        model.addAttribute("additionalFileId", additionalFileId);
        model.addAttribute("messageText", reviewEntity.getReviewMessage());
        model.addAttribute("files", chatAttachments);
        model.addAttribute("isOkStatus", isOkStatus);
        model.addAttribute("techNumber", techNumber);

        return "main/expert-review-order";
    }

    @GetMapping(value = "/review-order-answers")
    public String reviewOrderAnswers(Model model,
                                     @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "techNumber", required = false) String techNumber) {

        int pageCount = reviewService.getReviewsPageCount();
        page = Math.max(Math.min(pageCount, page), 1);
        List<Integer> pages = IntStream.range(1, pageCount + 1).boxed().toList();

        List<ReviewProjectionDto> all = List.of();
        if (techNumber != null) {
            all = reviewService.getAllReviews(techNumber);
        } else {
            all = reviewService.getAllReviews(page);
        }

        model.addAttribute("menuName", "Выберите заказ, который хотите посмотреть");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("reviews", all);
        model.addAttribute("showPages", true);
        model.addAttribute("page", page);
        model.addAttribute("pages", pages);

        return "main/expert-review-view-selector";
    }

    @GetMapping(value = "/expert-review-order-view/{orderId}/{reviewId}")
    public String expertReviewOrderView(Model model, @PathVariable Long orderId, @PathVariable Long reviewId) {
        OrderUiDto order = orderUiService.getOrder(orderId);

        ReviewDto reviewById = reviewService.getReviewById(reviewId);

        List<ReviewDto> olderReviews = reviewService.getOlderReviews(orderId).stream()
            .filter(t -> !t.getId().equals(reviewId))
            .toList();

        model.addAttribute("expertUsername", reviewById.getExpertUsername());
        model.addAttribute("menuName", "Вердикт проверки.");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("olderReviews", olderReviews);
        model.addAttribute("additionalFileId", reviewById.getAdditionalAttachmentId());
        model.addAttribute("review", reviewById);
        model.addAttribute("techNumber", order.getTechNumber());
        model.addAttribute("orderId", order.getId());

        return "main/expert-review-order-view";
    }

    @SneakyThrows
    @PostMapping(value = "/expert-review-order/{orderId}")
    public RedirectView expertReviewOrder(@PathVariable Long orderId,
                                          @RequestParam(value = "reviewId", required = false) String reviewId,
                                          @RequestParam("expertId") Long expertId,
                                          @RequestParam("attachmentId") Long attachmentId,
                                          @RequestParam("reviewText") String messageText,
                                          @RequestParam(value = "additionalFile", required = false) MultipartFile additionalFile) {


        AttachmentEntity additionalAttachment = null;

        if (additionalFile != null && StringUtils.isNotBlank(additionalFile.getOriginalFilename()) && additionalFile.getBytes().length > 0) {
            additionalAttachment = attachmentService.saveAttachment(additionalFile.getOriginalFilename(), additionalFile.getBytes());
        }

        if (reviewId == null) {
            reviewService.saveNewReviewRequest(orderId, expertId, attachmentId, messageText, additionalAttachment);
        } else {
            reviewService.updateReviewRequest(orderId, expertId, Long.parseLong(reviewId), attachmentId, messageText, additionalAttachment);
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
