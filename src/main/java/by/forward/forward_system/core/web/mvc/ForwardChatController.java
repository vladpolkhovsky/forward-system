package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.jpa.repository.CustomerTelegramToForwardOrderRepository;
import by.forward.forward_system.core.jpa.repository.projections.ForwardOrderProjection;
import by.forward.forward_system.core.jpa.repository.projections.ReviewRequestProjection;
import by.forward.forward_system.core.services.core.ForwardOrderService;
import by.forward.forward_system.core.services.core.ForwardOrderService.ForwardOrderData;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.ui.UserUiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Controller
@AllArgsConstructor
public class ForwardChatController {

    private final ForwardOrderService forwardOrderService;
    private final UserUiService userUiService;
    private final OrderService orderService;
    private final CustomerTelegramToForwardOrderRepository customerTelegramToForwardOrderRepository;

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

        ForwardOrderData userForwardOrderData = forwardOrderService.findAllProjections(currentUserId, isCurrentUserAdmin);

        Map<Long, LocalDateTime> chatIdToLastMessageDate = userForwardOrderData.chatLastMessageDate();
        Map<Long, Long> chatIdToMessageCount = userForwardOrderData.chatIdToMessageCount();

        List<ForwardOrderProjection> allProjections = userForwardOrderData.projections().stream()
            .sorted(createForwardOrderComparator(chatIdToLastMessageDate, chatIdToMessageCount))
            .toList();

        model.addAttribute("forwardOrders", allProjections);
        model.addAttribute("hasForwardOrders", !allProjections.isEmpty());
        model.addAttribute("idToMessageCount", chatIdToMessageCount);
        model.addAttribute("idToLastMessageDate", chatIdToLastMessageDate);

        Optional<ForwardOrderProjection> selectedForwardOrder = allProjections.stream()
            .filter(t -> Objects.equals(t.getId(), forwardOrderId))
            .findAny();

        if (forwardOrderId == null || chatId == null || selectedForwardOrder.isEmpty()) {

            model.addAttribute("hasOptionWindow", false);
            model.addAttribute("hasChatWindow", false);

            return "main/forward-order/forward-order-menu";
        }

        ForwardOrderProjection forwardOrder = selectedForwardOrder.get();

        Long forwardOrderCustomersCount = customerTelegramToForwardOrderRepository.countForwardOrderCustomers(forwardOrderId);

        OrderDto order = orderService.getOrder(forwardOrder.getOrderId());

        boolean isEnabledFileSubmission = forwardOrderService.isEnabledFileSubmission(forwardOrderId);
        boolean isPaymentSend = forwardOrder.getIsPaymentSend();

        model.addAttribute("forwardOrder", forwardOrder);
        model.addAttribute("forwardOrderId", forwardOrderId);
        model.addAttribute("forwardOrderCustomersCount", forwardOrderCustomersCount);
        model.addAttribute("orderInfo", order);

        model.addAttribute("hasOptionWindow", true);
        model.addAttribute("hasChatWindow", true);
        model.addAttribute("canAddReviewRequest", true);

        model.addAttribute("chatId", chatId);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("initJavascript", true);

        model.addAttribute("isEnabledFileSubmission", isEnabledFileSubmission);
        model.addAttribute("isPaymentSend", isPaymentSend);

        List<ReviewRequestProjection> reviewRequestsBy = forwardOrderService.findReviewRequestsBy(forwardOrderId);

        long approved = reviewRequestsBy.stream()
            .map(ReviewRequestProjection::getDone)
            .filter(BooleanUtils::isTrue)
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

    @NotNull
    private Comparator<ForwardOrderProjection> createForwardOrderComparator(Map<Long, LocalDateTime> chatIdToLastMessageDate, Map<Long, Long> chatIdToMessageCount) {
        Function<ForwardOrderProjection, LocalDateTime> getMaxDate = t -> ObjectUtils.max(
            chatIdToLastMessageDate.getOrDefault(t.getChatId(), t.getCreatedAt()),
            chatIdToLastMessageDate.getOrDefault(t.getAdminChatId(), t.getCreatedAt())
        );

        return Comparator
            .<ForwardOrderProjection>comparingLong(t -> getSumOfNewMessages(chatIdToMessageCount, t))
            .thenComparing(getMaxDate)
            .reversed();
    }

    private Long getSumOfNewMessages(Map<Long, Long> idToCount, ForwardOrderProjection projection) {
        return idToCount.getOrDefault(projection.getChatId(), 0L)
               + idToCount.getOrDefault(projection.getAdminChatId(), 0L);
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
                                            @RequestParam(value = "is-payment-send") Boolean isPaymentSend,
                                            @RequestParam(value = "redirect-url") String redirectUrl) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.changePaymentStatus(forwardOrderId, isPaymentSend, currentUserId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/change-file-submission-status/{forwardOrderId}")
    public RedirectView changeFileSubmissionStatus(@PathVariable Long forwardOrderId,
                                            @RequestParam(value = "allow-send-file") Boolean allowSendFile,
                                            @RequestParam(value = "redirect-url") String redirectUrl) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.changeFileSubmissionStatus(forwardOrderId, allowSendFile, currentUserId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/send-request-to-chat/{forwardOrderReviewRequestId}")
    public RedirectView sendRequestResultToChat(@PathVariable Long forwardOrderReviewRequestId,
                                            @RequestParam(value = "redirect-url") String redirectUrl) {
        forwardOrderService.sendRequestResultToChat(forwardOrderReviewRequestId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/save-author-note/{forwardOrderId}")
    public RedirectView saveAuthorNote(@PathVariable Long forwardOrderId,
                                       @RequestParam(value = "redirect-url") String redirectUrl,
                                       @RequestParam(value = "text") String text) {
        forwardOrderService.saveAuthorNote(forwardOrderId, text);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/save-admin-note/{forwardOrderId}")
    public RedirectView saveAdminNote(@PathVariable Long forwardOrderId,
                                       @RequestParam(value = "redirect-url") String redirectUrl,
                                       @RequestParam(value = "text") String text) {
        forwardOrderService.saveAdminNote(forwardOrderId, text);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/forward/delete-all-telegram-chat-customers/{forwardOrderId}")
    public RedirectView saveAdminNote(@PathVariable Long forwardOrderId,
                                      @RequestParam(value = "redirect-url") String redirectUrl) {
        Long currentUserId = userUiService.getCurrentUserId();
        forwardOrderService.deleteAllFromTelegramChat(forwardOrderId, currentUserId);
        return new RedirectView(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
    }


}
