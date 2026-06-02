/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
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
