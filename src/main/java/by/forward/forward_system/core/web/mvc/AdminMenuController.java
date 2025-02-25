package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.ui.UpdateOrderRequestDto;
import by.forward.forward_system.core.dto.ui.UserSelectionUiDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UpdateRequestOrderService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
@AllArgsConstructor
public class AdminMenuController {

    private final UpdateRequestOrderService updateRequestOrderService;

    private final UserUiService userUiService;

    private final OrderUiService orderUiService;
    private final OrderService orderService;

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

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("isViewed", byId.getIsViewed());
        model.addAttribute("requestId", requestId);
        model.addAttribute("menuName", "Укажите данные для перевода в статус \"В работе\"");
        model.addAttribute("authors", authors);
        model.addAttribute("catchers", catchers);
        model.addAttribute("hosts", hosts);
        model.addAttribute("orderId", byId.getOrderId());

        return "main/review-order";
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
