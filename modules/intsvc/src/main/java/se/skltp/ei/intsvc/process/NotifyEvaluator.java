package se.skltp.ei.intsvc.process;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyEvaluator implements ExpressionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(NotifyEvaluator.class);

    private static final String NAME = "ei-perform-notify";
    
    public String getName() {
    	log.debug("Return evaluator name {}", NAME);
        return NAME;
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("setName");
    }

    public Object evaluate(String expression, MuleMessage message) {
        try {
        	log.info("Evaluate: {} on message {}", expression, message.getPayload());

    		// FIXME - ML: True if Update or if PN and ET.size > 0

        	log.info("Evaluator return true");
        	return true;

        } catch (Exception e) {
        	log.warn("Evaluator failed, return true", e);
        	return true;
        }
    }
}