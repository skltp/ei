package se.skltp.ei;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

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

import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication(exclude = { JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class })
public class EiBackendApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(EiBackendApplication.class, args);
		handleDbStatus(ctx);
	}
	
	private static void handleDbStatus(ConfigurableApplicationContext ctx) {
		// Check if db is running
		boolean ignoreDbError = "true".equalsIgnoreCase(System.getProperty("ignoreDbError"));
		
		log.info("===========================================================");

		DataSource dataSource = ctx.getBean(DataSource.class);
		try(Connection conn = dataSource.getConnection()) {
			log.info("Database is available ... continue");
		} catch (SQLException e) {
			if(ignoreDbError)				
				log.error("Database is not available ... continue");
			else {
				log.error("Database is not available ... exiting");
				ctx.close();
			}
		} finally {
			log.info("===========================================================");		
		}
		
	}

}
