package by.forward.forward_system.core.web.mvc;

import by.forward.forward_system.core.enums.PaymentStatus;
import by.forward.forward_system.core.jpa.model.PaymentEntity;
import by.forward.forward_system.core.jpa.repository.projections.PaymentDto;
import by.forward.forward_system.core.services.core.PaymentService;
import by.forward.forward_system.core.services.ui.AuthorUiService;
import by.forward.forward_system.core.services.ui.UserUiService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.stream.LongStream;

@Controller
@AllArgsConstructor
public class AccountantController {

    private final UserUiService userUiService;

    private final AuthorUiService authorUiService;

    private final PaymentService paymentService;

    @GetMapping(value = "/create-payment")
    public String createPayment(Model model) {
        userUiService.checkAccessAccountant();

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("users", authorUiService.getAllAuthorsFast());

        return "main/accountant/create-payment";
    }

    @PostMapping(value = "/create-payment")
    public RedirectView initialSave(@RequestParam(value = "userId", required = true) Long userId,
                                    @RequestParam(value = "acc-message", required = true) String accMessage,
                                    @RequestParam(value = "acc-file", required = true) MultipartFile accFile
    ) {
        userUiService.checkAccessAccountant();

        Long currentUserId = userUiService.getCurrentUserId();

        paymentService.createPayment(userId, currentUserId, accMessage, accFile);

        return new RedirectView("/main");
    }

    @GetMapping(value = "/{type}-view-payment")
    public String viewPayment(@PathVariable String type, Model model, @RequestParam(value = "page", defaultValue = "1") long page) {
        if (!List.of("acc", "admin", "author").contains(type)) {
            throw new IllegalArgumentException("Неправильная ссылка!");
        }

        long pageCount = paymentService.countPages();
        if (type.equals("author")) {
            pageCount = paymentService.countPages(userUiService.getCurrentUserId());
        }

        page = Math.min(1, Math.max(page, pageCount));
        List<Long> pages = LongStream.range(1, pageCount + 1).boxed().toList();

        List<PaymentDto> payments = null;
        if (type.equals("author")) {
            payments = paymentService.getUserPayments(userUiService.getCurrentUserId(), page);
        } else {
            payments = paymentService.getAllPayments(page);
        }

        model.addAttribute("userShort", userUiService.getCurrentUser());
        model.addAttribute("pages", pages);
        model.addAttribute("page", page);
        model.addAttribute("type", type);
        model.addAttribute("payments", payments);

        return "main/accountant/payment-selector";
    }

    @GetMapping(value = "/payment/{paymentId}")
    public String payment(@PathVariable Long paymentId, Model model) {
        Long currentUserId = userUiService.getCurrentUserId();

        boolean isUser = paymentService.isPaymentUser(paymentId, currentUserId);
        boolean isAcc = userUiService.isCurrentUserOwner() || userUiService.isCurrentUserAccountant();

        PaymentEntity paymentEntity = paymentService.getPayment(paymentId);
        PaymentStatus paymentStatus = paymentEntity.getStatus().getStatus();
        Long paymentNumber = paymentEntity.getPaymentNumber();

        String username = paymentEntity.getUser().getUsername();
        String createdByUsername = paymentEntity.getCreatedByUser().getUsername();

        String annullReason = paymentEntity.getAnnulledReason();
        String accMessage = paymentEntity.getAccMessage();
        String userMessage = paymentEntity.getUserMessage();

        Long toSignFileId = paymentEntity.getAccAttachment().getId();
        Long signedFileId = paymentEntity.getUserAttachment() == null ? null : paymentEntity.getUserAttachment().getId();
        Long fileCheckId = paymentEntity.getUserCheckAttachment() == null ? null : paymentEntity.getUserCheckAttachment().getId();

        boolean isTimeToSendSigned = paymentStatus == PaymentStatus.WAITING_SIGNED_FILE;
        boolean isTimeToPay = paymentStatus == PaymentStatus.WAITING_PAYMENT;
        boolean isTimeToSendCheck = paymentStatus == PaymentStatus.WAITING_CHECK;
        boolean isVerification = paymentStatus == PaymentStatus.VERIFICATION;
        boolean isAnnulled = paymentStatus == PaymentStatus.ANNULLED;
        boolean isClosed = paymentStatus == PaymentStatus.CLOSED;

        model.addAttribute("userShort", userUiService.getCurrentUser());

        model.addAttribute("isUser", isUser);
        model.addAttribute("isAcc", isAcc);
        model.addAttribute("accMessage", accMessage);
        model.addAttribute("userMessage", userMessage);
        model.addAttribute("paymentNumber", paymentNumber);
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("isTimeToSendSigned", isTimeToSendSigned);
        model.addAttribute("isTimeToPay", isTimeToPay);
        model.addAttribute("isTimeToSendCheck", isTimeToSendCheck);
        model.addAttribute("isAnnulled", isAnnulled);
        model.addAttribute("annullReason", annullReason);
        model.addAttribute("isClosed", isClosed);
        model.addAttribute("isVerification", isVerification);
        model.addAttribute("username", username);
        model.addAttribute("createdByUsername", createdByUsername);
        model.addAttribute("toSignFileId", toSignFileId);
        model.addAttribute("signedFileId", signedFileId);
        model.addAttribute("fileCheckId", fileCheckId);

        return "main/accountant/payment";
    }

    @PostMapping(value = "/send-signed-payment/{paymentId}")
    public RedirectView saveSignedFile(@PathVariable Long paymentId,
                                       @RequestParam(value = "user-message", required = false) String userMessage,
                                       @RequestParam(value = "signed-file", required = true) MultipartFile signedFile) {
        userMessage = StringUtils.trimToNull(userMessage);
        paymentService.saveSignedFile(paymentId, userMessage, signedFile);
        return new RedirectView("/main");
    }

    @PostMapping(value = "/confirm-payment/{paymentId}")
    public RedirectView confirmPayment(@PathVariable Long paymentId) {
        paymentService.confirmPayment(paymentId);
        return new RedirectView("/main");
    }

    @PostMapping(value = "/send-check-payment/{paymentId}")
    public RedirectView saveCheckFile(@PathVariable Long paymentId,
                                      @RequestParam(value = "check-file", required = true) MultipartFile checkFile) {
        paymentService.saveCheckFile(paymentId, checkFile);
        return new RedirectView("/main");
    }

    @PostMapping(value = "/close-payment/{paymentId}")
    public RedirectView closePayment(@PathVariable Long paymentId) {
        paymentService.closePayment(paymentId);
        return new RedirectView("/main");
    }

    @PostMapping(value = "/annull-payment/{paymentId}")
    public RedirectView annullPayment(@PathVariable Long paymentId, @RequestParam(value = "annull-reason") String reason) {
        paymentService.annulPayment(paymentId, reason);
        return new RedirectView("/main");
    }
}
