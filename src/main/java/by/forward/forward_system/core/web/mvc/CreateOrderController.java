package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.ui.*;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.services.core.OrderService;
import by.forward.forward_system.core.services.core.UpdateRequestOrderService;
import by.forward.forward_system.core.services.ui.OrderUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CreateOrderController {

    private final UserUiService userUiService;

    private final OrderUiService orderUiService;

    private final OrderService orderService;

    private final UpdateRequestOrderService updateRequestOrderService;

    @GetMapping("/create-order")
    public String createOrder(Model model) {
        model.addAttribute("menuName", "Создать заказ");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", new OrderUiDto());
        model.addAttribute("actionUrl", "/create-order");
        return "main/create-order";
    }

    @GetMapping("/update-order")
    public String updateOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.CREATED.getName(),
            OrderStatus.DISTRIBUTION.getName()
        ));

        model.addAttribute("menuName", "Выберите заказ для изменения");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ordersList", allOrdersInStatus);
        return "main/update-order-selector";
    }

    @GetMapping("/update-order/{id}")
    public String updateOrder(Model model, @PathVariable Long id) {

        model.addAttribute("menuName", "Изменение заказа");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", orderUiService.getOrder(id));
        model.addAttribute("actionUrl", "/update-order/" + id);

        return "main/create-order";
    }

    @PostMapping(value = "/create-order", consumes = MediaType.ALL_VALUE)
    public String createOrder(@ModelAttribute("order") OrderUiDto order, Model model) {
        order = orderUiService.createOrder(order);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", order);
        model.addAttribute("users", orderUiService.getUserListWithCatcherMark(order.getId()));
        model.addAttribute("authors", orderUiService.getAuthorListWithAuthorMark(order.getId()));
        model.addAttribute("orderStatusName", orderUiService.getOrderStatus(order.getId()).getRusName());
        model.addAttribute("notDistributionStatus", !orderUiService.isDistributionStatus(order.getId()));
        model.addAttribute("orderCreated", order.getTechNumber());

        return "main/create-order-second-step";
    }

    @PostMapping(value = "/update-order/{id}", consumes = MediaType.ALL_VALUE)
    public String updateOrder(@PathVariable Long id, @ModelAttribute("order") OrderUiDto order, Model model) {
        order = orderUiService.updateOrder(id, order);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", order);
        model.addAttribute("users", orderUiService.getUserListWithCatcherMark(order.getId()));
        model.addAttribute("authors", orderUiService.getAuthorListWithAuthorMark(order.getId()));
        model.addAttribute("orderStatusName", orderUiService.getOrderStatus(order.getId()).getRusName());
        model.addAttribute("notDistributionStatus", !orderUiService.isDistributionStatus(order.getId()));
        model.addAttribute("orderUpdated", order.getTechNumber());

        return "main/create-order-second-step";
    }

    @GetMapping(value = "/view-order")
    public String viewOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.getAllOrders();
        model.addAttribute("menuName", "Выберите заказ для изменения");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ordersList", allOrdersInStatus);
        return "main/view-order-selector";
    }

    @GetMapping(value = "/view-order/{orderId}")
    public String viewOrder(Model model, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        List<OrderParticipantUiDto> participants = orderUiService.getAllParticipants(orderId);
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("order", order);
        model.addAttribute("participants", participants);
        return "main/view-order";
    }

    @GetMapping(value = "/order-to-in-progress")
    public String orderToInProgress(Model model) {
        model.addAttribute("userShort", userUiService.getCurrentUser());

        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.CREATED.getName(),
            OrderStatus.DISTRIBUTION.getName()
        ));

        model.addAttribute("menuName", "Выберите заказ для перевода в статус \"В работе\"");
        model.addAttribute("ordersList", allOrdersInStatus);
        return "main/to-in-progress-order-selector";
    }

    @GetMapping(value = "/order-to-in-progress/{orderId}")
    public String orderToInProgress(Model model, @PathVariable Long orderId) {
        model.addAttribute("userShort", userUiService.getCurrentUser());
        OrderUiDto order = orderUiService.getOrder(orderId);
        model.addAttribute("menuName", "Укажите данные для перевода в статус \"В работе\"");
        model.addAttribute("authors", orderUiService.getAuthorsByOrder(order.getId()));
        model.addAttribute("experts", orderUiService.getAllAuthors());
        model.addAttribute("catchers", orderUiService.getUserListWithCatcherMark(order.getId()));
        model.addAttribute("hosts", orderUiService.getAllManagers());
        model.addAttribute("orderId", orderId);
        return "main/to-in-progress-order";
    }

    @PostMapping(value = "/order-to-in-progress/{orderId}")
    public RedirectView orderToInProgress(@PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        OrderUiDto order = orderUiService.getOrder(orderId);
        UpdateOrderRequestDto updateOrderRequestDto = updateRequestOrderService.create(body, orderId, order.getTechNumber(), OrderStatus.ADMIN_REVIEW);
        updateRequestOrderService.save(updateOrderRequestDto);
        orderService.changeStatus(orderId, OrderStatus.DISTRIBUTION, OrderStatus.ADMIN_REVIEW);
        return new RedirectView("/order-to-in-progress");
    }

    @GetMapping(value = "/add-author-to-order")
    public String addAuthorToOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.REVIEW.getName(),
            OrderStatus.FINALIZATION.getName()
        ));
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите заказ в которого хотите добавить автора");
        model.addAttribute("ordersList", allOrdersInStatus);
        return "main/add-author-to-order-selector";
    }

    @GetMapping(value = "/add-author-to-order/{orderId}")
    public String addAuthorToOrder(Model model, @PathVariable Long orderId) {
        List<UserSelectionUiDto> allAuthors = orderUiService.getAllAuthors();
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите атвора, которого хотите добавить в заказ");
        model.addAttribute("authors", allAuthors);
        return "main/add-author-to-order";
    }

    @PostMapping(value = "/add-author-to-order/{orderId}")
    public RedirectView addAuthorToOrder(Model model, @PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        Long authorId = Long.parseLong(body.getFirst("author"));
        orderService.addMainAuthorToOrder(orderId, authorId);
        return new RedirectView("/change-fee-in-order");
    }

    @GetMapping(value = "/change-fee-in-order")
    public String changeFeeInOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.REVIEW.getName(),
            OrderStatus.FINALIZATION.getName()
        ));
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите заказ в котором хотите изменить доли авторов");
        model.addAttribute("ordersList", allOrdersInStatus);
        return "main/change-fee-in-order-selector";
    }

    @GetMapping(value = "/change-fee-in-order/{orderId}")
    public String changeFeeInOrder(Model model, @PathVariable Long orderId) {
        List<AuthorWithFeeDto> authorWithFeeDtos = orderUiService.getOrderAuthorsWithFee(orderId);
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Укажите доли авторов");
        model.addAttribute("authors", authorWithFeeDtos);
        model.addAttribute("order", orderUiService.getOrder(orderId));
        model.addAttribute("orderId", orderId);
        return "main/change-fee-in-order";
    }


    @PostMapping(value = "/change-fee-in-order/{orderId}")
    public RedirectView changeFeeInOrder(Model model, @PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        Map<Long, Integer> authorIdToFee = new HashMap<>();
        for (Map.Entry<String, List<String>> idToFee : body.entrySet()) {
            Long userId = Long.parseLong(idToFee.getKey().split("-")[1]);
            Integer fee = Integer.parseInt(idToFee.getValue().get(0));
            authorIdToFee.put(userId, fee);
        }
        orderService.applyFee(orderId, authorIdToFee);
        return new RedirectView("/main");
    }

}
