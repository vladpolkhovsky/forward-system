package by.forward.forward_system.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryListenerHandler extends MethodInvocationRetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.error("Error in @Retryable method labeled = {}. Error: ", callback.getLabel(), throwable);
        super.onError(context, callback, throwable);
    }
}
