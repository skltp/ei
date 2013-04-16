package se.skltp.ei.intsvc.dynamicFlows;

import java.util.Map;
import java.util.Map.Entry;

public interface Validator {
	
    /**
     * Validates a the properties as specified by propertiesToValidate
     * 
     * @param entry
     * @return
     */
    @SuppressWarnings("rawtypes")
	public boolean validateProperties(Map allProperties, Entry<Integer, Map> propertiesToValidate);
	

}
