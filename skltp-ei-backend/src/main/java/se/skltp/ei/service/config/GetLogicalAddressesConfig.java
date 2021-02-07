package se.skltp.ei.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "getlogicaladdresses")
public class GetLogicalAddressesConfig {
  String logicalAddress;
  String vpSenderId;
  String vpInstanceId;
  String serviceEndpointUrl;
  int connectTimeoutMs;
  int requestTimeoutMs;
}
