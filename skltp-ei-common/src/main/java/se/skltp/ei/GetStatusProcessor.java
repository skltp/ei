package se.skltp.ei;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.engine.DefaultRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class GetStatusProcessor implements Processor {

  public static final String KEY_APP_NAME = "Name";
  public static final String KEY_APP_VERSION = "Version";
  public static final String KEY_SPRING_PROFILES = "ActiveProfiles";
  public static final String KEY_APP_BUILD_TIME = "BuildTime";
  public static final String KEY_SERVICE_STATUS = "ServiceStatus";
  public static final String KEY_UPTIME = "Uptime";
  public static final String KEY_MANAGEMENT_NAME = "ManagementName";
  public static final String KEY_JAVA_VERSION = "JavaVersion";
  public static final String KEY_CAMEL_VERSION = "CamelVersion";
  public static final String KEY_JVM_TOTAL_MEMORY = "JvmTotalMemory";
  public static final String KEY_JVM_FREE_MEMORY = "JvmFreeMemory";
  public static final String KEY_JVM_USED_MEMORY = "JvmUsedMemory";
  public static final String KEY_JVM_MAX_MEMORY = "JvmMaxMemory";
  public static final String KEY_ENDPOINTS = "Endpoints";
  public static final String KEY_SERVICE_IMPLEMENTATIONS = "ServiceImplementations";

  @Autowired
  private CamelContext camelContext;

  @Autowired
  BuildProperties buildProperties;

  @Autowired
  private Environment environment;

  Map<String, String> implementationVersions;


  @Override
  public void process(Exchange exchange) {
    Map<String, Object> map = registerInfo();

    String json = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      DefaultPrettyPrinter p = new DefaultPrettyPrinter();
      p.indentArraysWith(new DefaultIndenter().withLinefeed(System.lineSeparator()));
      mapper.setDefaultPrettyPrinter(p);
      json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    } catch (JsonProcessingException e) {
      log.error("Error parsing Map to Json in GetStatusProcessor. Sending orinary string.");
      json = map.toString();
    }
    exchange.getIn().setBody(json.replace("\\/", "/"));
    exchange.getIn().getHeaders().put("Content-type", "application/json");
  }

  private Map<String, Object> registerInfo() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();

    map.put(KEY_APP_NAME, buildProperties.getName());
    map.put(KEY_APP_VERSION, buildProperties.getVersion());
    map.put(KEY_APP_BUILD_TIME, parseTimeValues(buildProperties.getTime()));

    ServiceStatus serviceStatus = camelContext.getStatus();
    map.put(KEY_SERVICE_STATUS, "" + serviceStatus);
    map.put(KEY_UPTIME, camelContext.getUptime());
    map.put(KEY_MANAGEMENT_NAME, camelContext.getManagementName());
    map.put(KEY_JAVA_VERSION, System.getProperties().get("java.version"));
    map.put(KEY_CAMEL_VERSION, camelContext.getVersion());

    Runtime instance = Runtime.getRuntime();
    map.put(KEY_JVM_TOTAL_MEMORY, "" + bytesReadable(instance.totalMemory()));
    map.put(KEY_JVM_FREE_MEMORY, "" + bytesReadable(instance.freeMemory()));
    map.put(KEY_JVM_USED_MEMORY, "" + bytesReadable((instance.totalMemory() - instance.freeMemory())));
    map.put(KEY_JVM_MAX_MEMORY, "" + bytesReadable(instance.maxMemory()));
    map.put(KEY_ENDPOINTS, getEndpointInfo());

    map.put(KEY_SPRING_PROFILES, environment.getActiveProfiles());

    if (implementationVersions != null) {
      map.put(KEY_SERVICE_IMPLEMENTATIONS, implementationVersions);
    }
    return map;
  }

  private String parseTimeValues(Instant time) {
    Date date = new Date(time.toEpochMilli());
    return getFormattedDate(date);
  }


  private List getEndpointInfo() {
    List<String> endPoints = new ArrayList<>();
    List<Route> routes = camelContext.getRoutes();
    for (Route route : routes) {
      String endpoint = route.getEndpoint().getEndpointKey();
      if (endpoint.contains("http://") && ((DefaultRoute) route).getStatus() == ServiceStatus.Started) {
        String key = route.getEndpoint().getEndpointKey();
        if (key.indexOf('?') > -1) {
          key = key.substring(0, key.indexOf('?'));
        }
        endPoints.add(key);
      }
    }
    return endPoints;
  }

  private String getFormattedDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    return date == null ? "" : dateFormat.format(date);
  }

  public static String bytesReadable(long v) {
    if (v < 1024) {
      return v + " B";
    }
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }
}

