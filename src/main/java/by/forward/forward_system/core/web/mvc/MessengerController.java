package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.ui.DeclineDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class MessengerController {

    private final UserUiService userUiService;

    private final ChatService chatService;

    private final OrderService orderService;

    @GetMapping(value = "/messenger")
    public String messenger(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("userId", userUiService.getCurrentUserId());
        model.addAttribute("isAdmin", userUiService.isCurrentUserAdmin());
        model.addAttribute("isOwner", userUiService.isCurrentUserOwner());
        return "messenger/messenger";
    }

    @GetMapping(value = "/new-messenger")
    public String newMessenger(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("userId", userUiService.getCurrentUserId());
        model.addAttribute("isAdmin", userUiService.isCurrentUserAdmin());
        model.addAttribute("isOwner", userUiService.isCurrentUserOwner());
        return "messenger/new-messenger";
    }

    @GetMapping(value = "/messenger-all-viewed")
    public RedirectView messengerAllViewed() {
        chatService.setAllMessagesViewed(userUiService.getCurrentUserId());
        return new RedirectView("/messenger?tab=new-orders");
    }

    @GetMapping(value = "/new-messenger-all-viewed")
    public RedirectView newMessengerAllViewed(@RequestParam(value = "tab", required = true) String tab) {
        chatService.setAllMessagesViewed(userUiService.getCurrentUserId(), tab);
        return new RedirectView("/new-messenger?tab=" + tab);
    }

    @GetMapping(value = "/request-order/{orderId}")
    public String requestOrder(Model model, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        boolean orderClosed = !order.getOrderStatus().equals(OrderStatus.DISTRIBUTION.getName());

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("order", order);
        model.addAttribute("decline", new DeclineDto());
        model.addAttribute("orderClosed", orderClosed);
        model.addAttribute("orderClosedShowError", orderClosed);

        return "messenger/request-order";
    }

    @GetMapping(value = "/order-info/{orderId}")
    public String orderInfo(Model model, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("order", order);
        model.addAttribute("decline", new DeclineDto());
        model.addAttribute("orderClosed", true);
        model.addAttribute("orderClosedShowError", false);

        return "messenger/request-order";
    }

}
