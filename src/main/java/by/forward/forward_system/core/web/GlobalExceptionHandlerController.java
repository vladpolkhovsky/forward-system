package by.forward.forward_system.core.web;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = Exception.class)
    public String handleAllException(HttpServletResponse httpServletResponse, Model model, Exception ex) {
        int code = 500;

        if (ex instanceof NoHandlerFoundException) {
            code = 404;
        }

        model.addAttribute("error", ExceptionUtils.getStackTrace(ex));
        model.addAttribute("code", code);

        httpServletResponse.setStatus(code);

        return "error";
    }

}
