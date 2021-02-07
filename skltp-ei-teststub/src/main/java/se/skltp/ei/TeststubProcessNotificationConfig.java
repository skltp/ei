package se.skltp.ei;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "teststub.notification")
public class TeststubProcessNotificationConfig {
    String expectedSenderId;
    String expectedInstanceId;
    String expectedLogicalAddress;
}
