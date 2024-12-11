package se.skltp.ei.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import jakarta.jms.JMSException;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsBrowser {

	private ActiveMQConnection conn;

	public JmsBrowser(ActiveMQConnectionFactory connectionFactory) throws JMSException {
		conn = (ActiveMQConnection) connectionFactory.createConnection();
    	conn.start();
	}
	
	public void browse() throws JMSException {
	    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	    	
	        Set<ActiveMQQueue> allque= conn.getDestinationSource().getQueues();

	        Iterator<ActiveMQQueue> itr= allque.iterator();
	        while(itr.hasNext()){
	          ActiveMQQueue q= itr.next();
	          System.out.println("| " + q.getQueueName() + " " + q.isTemporary());
	        }
	        
	    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	public Integer getQueueSize(String queueName) throws JMSException {
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        ActiveMQQueue q1 = new ActiveMQQueue(queueName);
		QueueBrowser browser = sess.createBrowser(q1);
        Enumeration<?> enu = browser.getEnumeration();
        int sum=0;   
          while (enu.hasMoreElements()) {
        	enu.nextElement();
            sum++;
           }
        return sum;	
	}
}
