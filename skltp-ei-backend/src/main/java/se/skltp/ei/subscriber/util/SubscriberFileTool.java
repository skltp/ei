package se.skltp.ei.subscriber.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import se.skltp.ei.service.util.JaxbUtil;
import se.skltp.ei.subscriber.Subscriber;

@Component
@Log4j2
public class SubscriberFileTool {

  private static final JaxbUtil JAXB = new JaxbUtil(PersistentCache.class);

  // cache
  @XmlRootElement
  static class PersistentCache implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement
    private List<Subscriber> subscribers;
  }

  private SubscriberFileTool() {
    // static utility
  }

  public static void saveToLocalCopy(List<Subscriber> subscribers, String subscriberCachefilePath) {

    if (subscriberCachefilePath == null || subscriberCachefilePath.isEmpty()) {
      log.warn("No subscriber local cache configured");
      return;
    }

    try( OutputStream os = new FileOutputStream(ResourceUtils.getFile(subscriberCachefilePath))) {

      PersistentCache persistentCache = new PersistentCache();
      persistentCache.subscribers = subscribers;
      os.write(JAXB.marshal(persistentCache).getBytes(StandardCharsets.UTF_8));
      log.info("Succesfully saved EI subscribers to local cache: {}", subscriberCachefilePath);

    } catch (Exception e) {
      log.error("Failed to save EI subscribers to local cache: {}", subscriberCachefilePath, e);
    }
  }


  public static List<Subscriber> restoreFromLocalCopy(String subscriberCachefilePath) {

    if (subscriberCachefilePath == null || subscriberCachefilePath.isEmpty()) {
      log.warn("No subscriber local cache configured");
      return Collections.emptyList();
    }

    try (InputStream is = new FileInputStream(ResourceUtils.getURL(subscriberCachefilePath).getFile())) {
      PersistentCache persistentCache = (PersistentCache) JAXB.unmarshal(inputStreamToString(is));
      if (persistentCache.subscribers != null) {

        log.info("Succesfully loaded EI subscribers to local cache: {}", subscriberCachefilePath);
        return persistentCache.subscribers;
      } else {
        log.warn("There is no EI subscribers available in local cache: {}", subscriberCachefilePath);
      }

    } catch (Exception e) {
      log.error("Failed to load EI subscribers from local cache: {}", subscriberCachefilePath,  e);
    }
    return Collections.emptyList();
  }

  private static String inputStreamToString(InputStream is)
          throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

}
