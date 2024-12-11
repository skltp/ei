package se.skltp.ei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
// TODO: Spring Boot 3.1.4 and 3.4.x reintroduces the AutoConfiguration for ActiveMQ.
//    v3.1 and 3.2 had a version that allowed partial AutoConfig, but excluded broker functions,
//       as that hadn't been updated to jakarta in some dependencies. I.o.w: a partial compatibility with old v5.8 AMQ logic.
//    v3.3 saw a full removal of AutoConfig as AMQ moved to v6, and dropped old support.
//    v3.4 will see the reintroduction of AMQ AutoConfig support for v6.
//    Might need to be excluded again, or reworked to depend on properties instead of manual setup.

@SpringBootApplication(exclude = { JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class})
public class EiFrontendApplication {
  public static void main(String[] args) {
    SpringApplication.run(EiFrontendApplication.class, args);
  }
}
