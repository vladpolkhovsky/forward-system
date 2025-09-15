package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.ui.AuthorWithFeeDto;
import by.forward.forward_system.core.dto.ui.DeclineDto;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.messager.ChatService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
@AllArgsConstructor
public class MessengerController {

    private final UserUiService userUiService;
    private final ChatService chatService;
    private final OrderService orderService;
    private final OrderUiService orderUiService;

    @Deprecated
    @GetMapping(value = "/messenger")
    public RedirectView messenger() {
        return new RedirectView("/new-messenger-v3");
    }

    @Deprecated
    @GetMapping(value = "/new-messenger")
    public RedirectView newMessenger(Model model) {
        return new RedirectView("/new-messenger-v3");
    }

    @GetMapping(value = "/new-messenger-v3")
    public String newMessengerV3(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        return "messenger-v3/messenger-v3";
    }

    @GetMapping(value = "/messenger-all-viewed")
    public RedirectView messengerAllViewed() {
        chatService.setAllMessagesViewed(userUiService.getCurrentUserId());
        return new RedirectView("/messenger?tab=new-orders");
    }

    @GetMapping(value = "/new-messenger-all-viewed")
    public RedirectView newMessengerAllViewed(@RequestParam(value = "tab") String tab) {
        chatService.setAllMessagesViewed(userUiService.getCurrentUserId(), tab);
        return new RedirectView("/new-messenger-v3?tab=" + tab + "#center");
    }

    @GetMapping(value = "/request-order/{orderId}")
    public String requestOrder(Model model, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);

        Integer authorsCost = order.getAuthorCost();
        List<AuthorWithFeeDto> orderAuthorsWithFee = orderUiService.getOrderAuthorsWithFee(orderId);
        for (AuthorWithFeeDto authorWithFee : orderAuthorsWithFee) {
            if (authorWithFee.getId().equals(userUiService.getCurrentUserId())) {
                authorsCost = authorWithFee.getFee();
            }
        }

        boolean orderClosed = !order.getOrderStatus().equals(OrderStatus.DISTRIBUTION.getName());

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("order", order);
        model.addAttribute("decline", new DeclineDto());
        model.addAttribute("orderClosed", orderClosed);
        model.addAttribute("authorsCost", authorsCost);
        model.addAttribute("orderClosedShowError", orderClosed);

        return "messenger/request-order";
    }

    @GetMapping(value = "/order-info/{orderId}")
    public String orderInfo(Model model, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);

        Integer authorsCost = order.getAuthorCost();
        List<AuthorWithFeeDto> orderAuthorsWithFee = orderUiService.getOrderAuthorsWithFee(orderId);
        for (AuthorWithFeeDto authorWithFee : orderAuthorsWithFee) {
            if (authorWithFee.getId().equals(userUiService.getCurrentUserId())) {
                authorsCost = authorWithFee.getFee();
            }
        }

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("order", order);
        model.addAttribute("decline", new DeclineDto());
        model.addAttribute("orderClosed", true);
        model.addAttribute("authorsCost", authorsCost);
        model.addAttribute("orderClosedShowError", false);

        return "messenger/request-order";
    }

}
