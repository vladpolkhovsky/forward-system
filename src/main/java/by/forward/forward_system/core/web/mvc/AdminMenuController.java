package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UpdateOrderRequestDto;
import by.forward.forward_system.core.dto.ui.UserSelectionUiDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.jpa.repository.projections.UserPlanProjectionDto;
import by.forward.forward_system.core.jpa.repository.projections.UserSimpleProjectionDto;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.PlanService;
import by.forward.forward_system.core.services.core.UpdateRequestOrderService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class AdminMenuController {

    private final UpdateRequestOrderService updateRequestOrderService;

    private final UserUiService userUiService;

    private final OrderUiService orderUiService;

    private final OrderService orderService;
    private final PlanService planService;

    @GetMapping(value = "/order-send-log")
    public String getDistributionLogs() {
        userUiService.checkAccessAdmin();
        return "main/admin/order-send-log";
    }

    @GetMapping(value = "/add-manager-to-chat")
    public String addManagerToChat() {
        userUiService.checkAccessAdmin();
        return "main/admin/add-manager-to-chat";
    }

    @GetMapping(value = "/order-review")
    public String orderReview(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите запрос на подтверждение");
        List<UpdateOrderRequestDto> notReviewedOrderRequests = updateRequestOrderService.getNotReviewedOrderRequests();
        model.addAttribute("requests", notReviewedOrderRequests);

        return "main/review-order-selector";
    }

    @GetMapping(value = "/review-order/{requestId}")
    public String reviewOrder(Model model, @PathVariable Long requestId) {
        userUiService.checkAccessAdmin();

        UpdateOrderRequestDto byId = updateRequestOrderService.getById(requestId);

        List<UserSelectionUiDto> authors = setChecked(orderUiService.getAuthorsByOrder(byId.getOrderId()), byId.getAuthors());
        List<UserSelectionUiDto> hosts = setChecked(orderUiService.getAllManagers(), byId.getHosts());
        List<UserSelectionUiDto> catchers = setChecked(orderUiService.getAllManagers(), byId.getCatchers());

        UserSelectionUiDto selectedCatcher = catchers.stream()
            .filter(UserSelectionUiDto::getChecked)
            .findFirst()
            .orElse(null);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("isViewed", byId.getIsViewed());
        model.addAttribute("isForwardOrder", byId.getIsForwardOrder());
        model.addAttribute("requestId", requestId);
        model.addAttribute("menuName", "Укажите данные для перевода в статус \"В работе\"");
        model.addAttribute("authors", authors);
        model.addAttribute("catchers", catchers);
        model.addAttribute("hosts", hosts);
        model.addAttribute("orderId", byId.getOrderId());
        model.addAttribute("selectedCatcher", selectedCatcher);

        return "main/review-order";
    }

    @GetMapping("/review-order-redirect-to-chat/{catcherId}/{authorId}")
    public RedirectView reviewOrderRedirectWhenClickShowChat(@PathVariable Long catcherId, @PathVariable Long authorId) {
        Long chatId = userUiService.getChatIdWithManagerAndAuthor(catcherId, authorId);
        return new RedirectView("/new-messenger-v3?tab=all&chatId=%d".formatted(chatId));
    }

    @GetMapping(value = "/create-plan")
    public String createPlan(Model model) {
        userUiService.checkAccessAdmin();

        model.addAttribute("userShort", userUiService.getCurrentUser());

        return "main/plan/create-plan";
    }

    @PostMapping(value = "/create-plan")
    @Transactional
    public RedirectView createPlan(
        @RequestParam(value = "userId") Long targetUserId,
        @RequestParam(value = "username") String username,
        @RequestParam(value = "plan-start") String planStart,
        @RequestParam(value = "plan-end") String planEnd,
        @RequestParam(value = "plan-amount") Long planTarget,
        @RequestParam(value = "plan-amount-count") Long planTargetCount
    ) {
        userUiService.checkAccessAdmin();

        Long createdBy = userUiService.getCurrentUserId();

        LocalDateTime start = LocalDate.parse(planStart, DateTimeFormatter.ISO_DATE).atStartOfDay();
        LocalDateTime end = LocalDate.parse(planEnd, DateTimeFormatter.ISO_DATE).atStartOfDay().minusSeconds(1);

        planService.savePlan(targetUserId, start, end, planTarget, planTargetCount, createdBy);

        return new RedirectView("/main");
    }

    @GetMapping(value = "/calendar-constructor")
    public String getCalendarConstructor(Model model) {
        userUiService.checkAccessAdmin();
        model.addAttribute("userShort", userUiService.getCurrentUser());
        return "main/calendar/calendar-constructor";
    }

    @GetMapping(value = "/view-plan")
    public String planRemove(Model model) {
        userUiService.checkAccessAdmin();

        List<UserSimpleProjectionDto> managers = planService.getAllManagers();
        List<Long> managerIds = managers.stream()
            .map(UserSimpleProjectionDto::getId)
            .toList();

        Map<Long, List<UserPlanProjectionDto>> plansByManager = planService.getAllUsersPlans(managerIds).stream()
            .collect(Collectors.groupingBy(UserPlanProjectionDto::getUserId))
            .entrySet().stream()
            .map(e -> {
                List<UserPlanProjectionDto> sorted = e.getValue().stream()
                    .sorted(Comparator.comparing(UserPlanProjectionDto::getStartDateTime).reversed())
                    .toList();
                sorted = sorted.subList(Math.max(0, sorted.size() - 4), sorted.size());
                return Map.entry(
                    e.getKey(),
                    sorted
                );
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Long, PlanService.UserPlanDetailsDto> planDetailsByPlanId = plansByManager.values().stream()
            .flatMap(Collection::stream)
            .parallel()
            .map(planService::userPlanToDetails)
            .collect(Collectors.toMap(PlanService.UserPlanDetailsDto::planId, userPlanDetailsDto -> userPlanDetailsDto));

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("managers", managers);
        model.addAttribute("plansByManager", plansByManager);
        model.addAttribute("planDetailsByPlanId", planDetailsByPlanId);
        model.addAttribute("now", LocalDateTime.now());

        return "main/plan/view-plan";
    }

    @PostMapping(value = "/delete-plan/{planId}")
    @Transactional
    public RedirectView planRemove(@PathVariable Long planId) {
        userUiService.checkAccessAdmin();

        planService.deletePlan(planId);

        return new RedirectView("/view-plan");
    }

    @PostMapping(value = "/review-order/{requestId}")
    @Transactional
    public RedirectView reviewOrder(@PathVariable Long requestId, @RequestBody MultiValueMap<String, String> body) {
        userUiService.checkAccessAdmin();

        UpdateOrderRequestDto byId = updateRequestOrderService.getById(requestId);

        if (byId.getIsViewed()) {
            return new RedirectView("/main");
        }

        Boolean verdict = BooleanUtils.toBoolean(body.getFirst("verdict"));

        UpdateOrderRequestDto created = updateRequestOrderService.create(
            body,
            byId.getOrderId(),
            byId.getOrderTechNumber(),
            OrderStatus.byName(byId.getNewStatus())
        );

        UpdateOrderRequestDto update = updateRequestOrderService.update(requestId, created, true, verdict);

        orderService.applyUpdateOrderRequest(update);

        return new RedirectView("/main");
    }

    private List<UserSelectionUiDto> setChecked(List<UserSelectionUiDto> userSelectionUiDtos, List<Long> checkedIds) {
        return userSelectionUiDtos.stream()
            .peek(t -> t.setChecked(checkedIds.contains(t.getId())))
            .toList();
    }
}
