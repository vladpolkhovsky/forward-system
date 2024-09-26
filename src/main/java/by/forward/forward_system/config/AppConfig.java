package by.forward.forward_system.config;

import by.forward.forward_system.core.jpa.repository.MessageRepository;
import by.forward.forward_system.core.services.messager.MessageCounter;
import by.forward.forward_system.core.services.ui.UserUiService;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean("newMsgCount")
    public MessageCounter newMsgCount(@Autowired MessageRepository messageRepository, @Autowired UserUiService userUiService) {
        return new MessageCounter(messageRepository, userUiService);
    }

    @Bean
    public AmazonS3 yandexCloudObjectStorage() {
        AWSCredentials credentials;

        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Не получилось создать объект credentials (~/.aws/credentials)", e);
        }

        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(
                new AmazonS3ClientBuilder.EndpointConfiguration(
                    "storage.yandexcloud.net","ru-central1"
                )
            )
            .build();
    }

}
