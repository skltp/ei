package se.skltp.ei;

import javax.jms.ConnectionFactory;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class EiBrokerConfiguration  {

  @Value("${activemq.broker.url:}")
  String activemqBrokerUrl;

  @Value("${activemq.broker.user:#{null}}")
  String activemqBrokerUser;

  @Value("${activemq.broker.password:#{null}}")
  String activemqBrokerPassword;

  @Bean
  ActiveMQConnectionFactory amqConnectionFactory() {
    final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    activeMQConnectionFactory.setBrokerURL(activemqBrokerUrl);
    if(activemqBrokerUser != null && activemqBrokerUser.length() > 0 ) {    	
        activeMQConnectionFactory.setUserName(activemqBrokerUser);
        activeMQConnectionFactory.setPassword(activemqBrokerPassword); 
    }
    return activeMQConnectionFactory;
  }

	@Bean
	@Primary
	public PooledConnectionFactory pooledConnectionFactory(ConnectionFactory cf) {
	    final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
	    pooledConnectionFactory.setMaxConnections(2);
	    pooledConnectionFactory.setConnectionFactory(cf);
	    return pooledConnectionFactory;
	}
	
	@Bean(name = "activemq")
	@ConditionalOnClass(ActiveMQComponent.class)
	public ActiveMQComponent activeMQComponent(ConnectionFactory connectionFactory) {
	    ActiveMQComponent activeMQComponent = new ActiveMQComponent();
	    activeMQComponent.setConnectionFactory(connectionFactory);
	    activeMQComponent.setTransacted(true);
	    activeMQComponent.setLazyCreateTransactionManager(false);
	    return activeMQComponent;
	}
	

}
