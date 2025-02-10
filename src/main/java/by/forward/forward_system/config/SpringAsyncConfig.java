package by.forward.forward_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer {

    @Autowired
    @Qualifier("springAsyncTaskExecutor")
    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Override
    public Executor getAsyncExecutor() {
        return poolTaskExecutor;
    }
}
