package by.forward.forward_system.config;

import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.services.messager.MessageCounter;
import by.forward.forward_system.core.services.ui.UserUiService;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class AppConfig {

    @Value("${aws.aws_access_key_id}")
    private String awsAccessKey;

    @Value("${aws.aws_secret_access_key}")
    private String awsSecretKey;

    @Value("${telegram.bot_token}")
    private String botToken;

    @Value("${site.domain}")
    private String siteDomain;

    @Value("${management.endpoints.web.shutdown-path}")
    private String shutdownUrl;

    @Value("${management.emergency-menu-url}")
    private String emergencyMenuUrl;

    @Value("${secret.emergency_password}")
    private String secretPassword;

    @Bean("shutdownUrl")
    public String shutdownUrl() {
        return shutdownUrl;
    }

    @Bean("secretPassword")
    public String secretPassword() {
        return secretPassword;
    }

    @Bean("telegramToken")
    public String telegramToken() {
        return botToken;
    }

    @Bean("siteDomain")
    public String siteDomain() {
        return siteDomain;
    }

    @Bean("emergencyMenuUrl")
    public String emergencyMenuUrl() {
        return emergencyMenuUrl;
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean("newMsgCount")
    public MessageCounter newMsgCount(@Autowired MessageRepository messageRepository, @Autowired UserUiService userUiService) {
        return new MessageCounter(messageRepository, userUiService);
    }

    @Bean
    public AmazonS3 yandexCloudObjectStorage() {
        AWSCredentials credentials;

        try {
            credentials = new BasicSessionCredentials(awsAccessKey, awsSecretKey, null);
        } catch (Exception e) {
            throw new AmazonClientException("Не получилось создать объект credentials (~/.aws/credentials)", e);
        }

        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(
                new AmazonS3ClientBuilder.EndpointConfiguration(
                    "storage.yandexcloud.net", "ru-central1"
                )
            )
            .build();
    }

    @Bean
    @Qualifier("springAsyncTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("async-thread-");
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setMaxPoolSize(2);
        threadPoolTaskExecutor.setQueueCapacity(120);
        return threadPoolTaskExecutor;
    }
}
