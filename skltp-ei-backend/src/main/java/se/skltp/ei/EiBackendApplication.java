package se.skltp.ei;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
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
		// Check if db is runnimg
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
