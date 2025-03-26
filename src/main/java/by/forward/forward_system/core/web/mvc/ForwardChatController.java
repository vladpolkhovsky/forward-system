package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.jpa.repository.ForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.ForwardOrderProjection;
import by.forward.forward_system.core.jpa.repository.projections.ReviewRequestProjection;
import by.forward.forward_system.core.services.core.ForwardOrderService;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.ui.UserUiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@AllArgsConstructor
public class ForwardChatController {

    private final ForwardOrderService forwardOrderService;
    private final UserUiService userUiService;
    private final ForwardOrderRepository forwardOrderRepository;
    private final OrderService orderService;

    @GetMapping("/forward/main")
    public String forwardChatsMenu(Model model,
                                   @RequestParam(value = "forwardOrderId", required = false) Long forwardOrderId,
                                   @RequestParam(value = "chatId", required = false) Long chatId,
                                   HttpServletRequest request
    ) {
        String contextPath = request.getRequestURI() + "?" + request.getQueryString() + "#main-view";
        String redirectUrl = URLEncoder.encode(contextPath, StandardCharsets.UTF_8);

        boolean isCurrentUserAdmin = userUiService.isCurrentUserAdmin();
        long currentUserId = userUiService.getCurrentUserId();

        model.addAttribute("userShort", userUiService.getCurrentUser());

        model.addAttribute("isAdmin", isCurrentUserAdmin);
        model.addAttribute("redirectUrl", redirectUrl);

        Map<Long, Long> chatIdToMessageCount = forwardOrderService.newMessageCount(currentUserId);

        List<ForwardOrderProjection> allProjections = forwardOrderService.findAllProjections(currentUserId, isCurrentUserAdmin);
        allProjections = allProjections.stream()
            .sorted(Comparator.comparing(t -> getSumOfNewMessages(chatIdToMessageCount, t)))
            .toList();

        model.addAttribute("forwardOrders", allProjections);
        model.addAttribute("hasForwardOrders", !allProjections.isEmpty());
        model.addAttribute("idToMessageCount", chatIdToMessageCount);

        if (forwardOrderId == null || chatId == null) {

            model.addAttribute("hasOptionWindow", false);
            model.addAttribute("hasChatWindow", false);

            return "main/forward-order/forward-order-menu";
        }

        ForwardOrderProjection forwardOrder = allProjections.stream()
            .filter(t -> Objects.equals(t.getId(), forwardOrderId))
            .findAny()
            .get();

        OrderDto order = orderService.getOrder(forwardOrder.getOrderId());

        boolean isEnabledFileSubmission = forwardOrderRepository.isEnabledFileSubmission(forwardOrderId);

        model.addAttribute("forwardOrder", forwardOrder);
        model.addAttribute("forwardOrderId", forwardOrderId);
        model.addAttribute("orderInfo", order);

        model.addAttribute("hasOptionWindow", true);
        model.addAttribute("hasChatWindow", true);
        model.addAttribute("canAddReviewRequest", true);

        model.addAttribute("chatId", chatId);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("initJavascript", true);

        model.addAttribute("isEnabledFileSubmission", isEnabledFileSubmission);

        List<ReviewRequestProjection> reviewRequestsBy = forwardOrderService.findReviewRequestsBy(forwardOrderId);

        long approved = reviewRequestsBy.stream()
            .filter(ReviewRequestProjection::getDone)
            .count();

        long reviewed = reviewRequestsBy.stream()
            .map(ReviewRequestProjection::getReviewIsReviewed)
            .filter(BooleanUtils::isTrue)
            .count();

        model.addAttribute("reviewRequests", reviewRequestsBy);
        model.addAttribute("reviewRequestsApproved", approved);
        model.addAttribute("reviewRequestsReviewed", reviewed);

        return "main/forward-order/forward-order-menu";
    }

    @PostMapping("/forward/save-forward-order-review-request/{forwardOrderId}")
    public RedirectView saveSignedFile(@PathVariable Long forwardOrderId,
                                       @RequestParam(value = "redirect-url") String redirectUrl,
                                       @RequestParam(value = "request-note") String userNote,
                                       @RequestParam(value = "request-file") MultipartFile requestFile) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.saveReviewRequest(forwardOrderId, userNote, currentUserId, requestFile);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/delete-request/{forwardOrderId}/{forwardOrderReviewRequestId}")
    public RedirectView deleteRequest(@PathVariable Long forwardOrderId,
                                      @PathVariable Long forwardOrderReviewRequestId,
                                      @RequestParam(value = "redirect-url") String redirectUrl) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.deleteForwardOrderReviewRequest(forwardOrderId, forwardOrderReviewRequestId, currentUserId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/change-payment-status/{forwardOrderId}")
    public RedirectView changePaymentStatus(@PathVariable Long forwardOrderId,
                                            @RequestParam(value = "allow-send-file") Boolean allowSendFile,
                                            @RequestParam(value = "redirect-url") String redirectUrl) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.changePaymentStatus(forwardOrderId, allowSendFile, currentUserId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/send-request-to-chat/{forwardOrderReviewRequestId}")
    public RedirectView sendRequestResultToChat(@PathVariable Long forwardOrderReviewRequestId,
                                            @RequestParam(value = "redirect-url") String redirectUrl) {
        forwardOrderService.sendRequestResultToChat(forwardOrderReviewRequestId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    private Long getSumOfNewMessages(Map<Long, Long> idToCount, ForwardOrderProjection projection) {
        return idToCount.getOrDefault(projection.getChatId(), 0L)
               + idToCount.getOrDefault(projection.getAdminChatId(), 0L);
    }

}
