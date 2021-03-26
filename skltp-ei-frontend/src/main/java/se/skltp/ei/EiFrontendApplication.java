package se.skltp.ei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;

@SpringBootApplication(exclude = { JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class})
public class EiFrontendApplication {
  public static void main(String[] args) {
    SpringApplication.run(EiFrontendApplication.class, args);
  }
}
