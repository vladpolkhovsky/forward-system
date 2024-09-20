package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.dto.messenger.OrderDto;
import by.forward.forward_system.core.dto.ui.*;
import by.forward.forward_system.core.enums.OrderStatus;
import by.forward.forward_system.core.services.core.DisciplineService;
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
import org.springframework.web.multipart.MultipartFile;
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

    private final DisciplineService disciplineService;

    @GetMapping("/create-order")
    public String createOrder(Model model) {
        userUiService.checkAccessManager();

        model.addAttribute("menuName", "Создать заказ");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", new OrderUiDto());
        model.addAttribute("actionUrl", "/create-order");
        model.addAttribute("lastTechNumber", orderUiService.getLastTechNumber());
        model.addAttribute("disciplines", disciplineService.allDisciplines());

        return "main/create-order";
    }

    @PostMapping(value = "/create-order", consumes = MediaType.ALL_VALUE)
    public RedirectView createOrder(@ModelAttribute("order") OrderUiDto order, Model model) {
        userUiService.checkAccessManager();

        boolean isValidData = orderUiService.checkOrderByAi(order);

        if (!isValidData) {
            return new RedirectView("/ban");
        }

        order = orderUiService.createOrder(order);

        return new RedirectView("/files-order/" + order.getId());
    }

    @GetMapping("/update-order")
    public String updateOrder(Model model) {
        userUiService.checkAccessManager();

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
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        model.addAttribute("menuName", "Изменение заказа");
        model.addAttribute("disciplines", disciplineService.allDisciplines());
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", orderUiService.getOrder(id));
        model.addAttribute("lastTechNumber", orderUiService.getLastTechNumber());
        model.addAttribute("actionUrl", "/update-order/" + id);

        return "main/create-order";
    }

    @PostMapping(value = "/update-order/{id}", consumes = MediaType.ALL_VALUE)
    public RedirectView updateOrder(@PathVariable Long id, @ModelAttribute("order") OrderUiDto order, Model model) {
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        boolean isValidData = orderUiService.checkOrderByAi(order);

        if (!isValidData) {
            return new RedirectView("/ban");
        }

        order = orderUiService.updateOrder(id, order);

        return new RedirectView("/files-order/" + id);
    }

    @GetMapping("/order-second-step/{id}")
    public String orderSecondStep(@PathVariable Long id, Model model) {
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        OrderUiDto order = orderUiService.getOrder(id);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("order", order);
        model.addAttribute("users", orderUiService.getUserListWithCatcherMark(order.getId()));
        model.addAttribute("authors", orderUiService.getAuthorListWithAuthorMark(order.getId()));
        model.addAttribute("orderStatusName", orderUiService.getOrderStatus(order.getId()).getRusName());
        model.addAttribute("notDistributionStatus", !orderUiService.isDistributionStatus(order.getId()));

        return "main/create-order-second-step";
    }


    @GetMapping("/files-order/{id}")
    public String orderFiles(@PathVariable Long id, Model model) {
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Фыйлы для заказа");
        model.addAttribute("files", orderService.getOrderAttachments(id));
        model.addAttribute("orderId", id);

        return "main/order-files";
    }

    @PostMapping("/order-file-delete/{id}/{attId}")
    public RedirectView orderFileDelete(@PathVariable Long id, @PathVariable Long attId) {
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        orderService.removeOrderFile(id, attId);

        return new RedirectView("/files-order/" + id);
    }

    @PostMapping("/files-order/{id}")
    public String saveOrderFile(@PathVariable Long id, @RequestParam("file") MultipartFile[] file, Model model) {
        orderService.checkOrderAccessEdit(id, userUiService.getCurrentUserId());

        orderService.saveOrderFile(id, file);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Файлы для заказа");
        model.addAttribute("files", orderService.getOrderAttachments(id));
        model.addAttribute("orderId", id);

        return "main/order-files";
    }

    @GetMapping(value = "/view-order")
    public String viewOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.getAllOrders();

        model.addAttribute("menuName", "Выберите заказ для просмотра");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ordersList", allOrdersInStatus);

        return "main/view-order-selector";
    }

    @GetMapping(value = "/view-order/{orderId}")
    public String viewOrder(Model model, @PathVariable Long orderId) {
        orderService.checkOrderAccessView(orderId, userUiService.getCurrentUserId());

        OrderDto order = orderService.getOrder(orderId);
        Long orderMainChatId = orderService.getOrderMainChat(orderId);
        List<OrderParticipantUiDto> participants = orderUiService.getAllParticipants(orderId);

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Заказ №" + order.getTechNumber());
        model.addAttribute("hasMainChat", orderMainChatId != null);
        model.addAttribute("mainChatId", orderMainChatId);
        model.addAttribute("order", order);
        model.addAttribute("participants", participants);

        return "main/view-order";
    }

    @GetMapping(value = "/view-my-order")
    public String viewMyOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.getAllMyOrders(userUiService.getCurrentUserId());

        model.addAttribute("menuName", "Выберите заказ для просмотра");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ordersList", allOrdersInStatus);

        return "main/view-order-selector";
    }

    @GetMapping(value = "/view-my-order-author")
    public String viewMyOrderAuthor(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.getAllMyOrders(userUiService.getCurrentUserId());

        model.addAttribute("menuName", "Выберите заказ для просмотра");
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("ordersList", allOrdersInStatus);

        return "main/view-order-author-selector";
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
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        OrderUiDto order = orderUiService.getOrder(orderId);

        model.addAttribute("userShort", userUiService.getCurrentUser());
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
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

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
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        List<UserSelectionUiDto> allAuthors = orderUiService.getAllAuthors();
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите автора, которого хотите добавить в заказ");
        model.addAttribute("authors", allAuthors);

        return "main/add-author-to-order";
    }

    @PostMapping(value = "/add-author-to-order/{orderId}")
    public RedirectView addAuthorToOrder(Model model, @PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        Long authorId = Long.parseLong(body.getFirst("author"));
        orderService.addMainAuthorToOrder(orderId, authorId);

        return new RedirectView("/change-fee-in-order");
    }

    @GetMapping(value = "/del-author-from-order")
    public String delAuthorFromOrder(Model model) {
        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.REVIEW.getName(),
            OrderStatus.FINALIZATION.getName()
        ));

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите заказ в которого хотите добавить автора");
        model.addAttribute("ordersList", allOrdersInStatus);

        return "main/del-author-from-order-selector";
    }

    @GetMapping(value = "/del-author-from-order/{orderId}")
    public String delAuthorFromOrder(Model model, @PathVariable Long orderId) {
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        List<UserSelectionUiDto> allAuthors = orderUiService.getAuthorsByOrder(orderId);
        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите автора, которого хотите удалить из заказа");
        model.addAttribute("isOneAuthor", orderUiService.countOrderAuthors(orderId) <= 1);
        model.addAttribute("authors", allAuthors);

        return "main/del-author-from-order";
    }

    @PostMapping(value = "/del-author-from-order/{orderId}")
    public RedirectView delAuthorFromOrder(Model model, @PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        Long authorId = Long.parseLong(body.getFirst("author"));
        orderService.delMainAuthorFromOrder(orderId, authorId);

        return new RedirectView("/main");
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
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

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
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        Map<Long, Integer> authorIdToFee = new HashMap<>();
        for (Map.Entry<String, List<String>> idToFee : body.entrySet()) {
            Long userId = Long.parseLong(idToFee.getKey().split("-")[1]);
            Integer fee = Integer.parseInt(idToFee.getValue().get(0));
            authorIdToFee.put(userId, fee);
        }

        orderService.applyFee(orderId, authorIdToFee);

        return new RedirectView("/main");
    }

    @GetMapping(value = "change-order-status")
    public String changeOrderStatusSelector(Model model) {
        userUiService.checkAccessManager();

        List<OrderUiDto> allOrdersInStatus = orderUiService.findAllOrdersInStatus(Arrays.asList(
            OrderStatus.IN_PROGRESS.getName(),
            OrderStatus.REVIEW.getName(),
            OrderStatus.FINALIZATION.getName(),
            OrderStatus.GUARANTEE.getName(),
            OrderStatus.CLOSED.getName()
        ));

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Выберите заказ в котором хотите изменить статус");
        model.addAttribute("ordersList", allOrdersInStatus);

        return "main/change-order-status-selector";
    }

    @GetMapping(value = "change-order-status/{orderId}")
    public String changeOrderStatus(Model model, @PathVariable Long orderId) {
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("menuName", "Измените статус заказа");
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("order", orderUiService.getOrder(orderId));
        model.addAttribute("orderId", orderId);

        return "main/change-order-status";
    }

    @PostMapping(value = "/change-order-status/{orderId}")
    public RedirectView changeOrderStatusSelector(@PathVariable Long orderId, @RequestBody MultiValueMap<String, String> body) {
        orderService.checkOrderAccessEdit(orderId, userUiService.getCurrentUserId());

        String status = body.getFirst("status");
        orderService.changeStatus(orderId, OrderStatus.byName(status));

        return new RedirectView("/main");
    }

}
