package se.skltp.ei.intsvc.dynamicFlows;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PropertyUtil {

	// Pattern for a multiple PARTNER parameter: PARTNER(n)_(name)
    public static final String PARTNER_LIST_NAME     = "AGMTS"; // The map-key for agreements, does not exist in the property-file
    public static final String PARTNER_PREFIX        = "AGMT";
    public static final int    PARTNER_PREFIX_LENGTH = 4; // todo: PARTNER_PREFIX.length(); gives 0 ???
    public static final String PARTNER_INBOUND_MANAGE_STOPFILE = "INBOUND_MANAGE_STOPFILE";
    public static final String PARTNER_INBOUND_FOLDER = "INBOUND_FOLDER";
    
    public static final String ID                    = "ID";
    public static final char   SEPARATOR             = '_';

    public static final String SUPERVISE_FILES_LIST_NAME = "SUPERVISE_FILES";  // The map-key for files to supervise, does not exist in the property-file
    public static final String SUPERVISE_FILES_PREFIX    = "SUPERVISE_FILES_";
    public static final int    SUPERVISE_FILES_PREFIX_LENGTH = 16; // todo: SUPERVISE_FILES_PREFIX.length(); gives 0 ???
    public static final String SUPERVISE_FILES_FOLDERS   = "FOLDERS";
    public static final String SUPERVISE_FILES_MAXAGE    = "MAXAGE_MINUTES";
    public static final String SUPERVISE_FILES_FILTER    = "FILE_FILTER";
    public static final String SUPERVISE_FILES_STARTTIME = "STARTTIME";
    public static final String SUPERVISE_FILES_STOPTIME  = "STOPTIME";

    public static final String SUPERVISE_ACTIVEMQ_LIST_NAME       = "SUPERVISE_AMQ_BROKERS"; // The map-key for amq brokers to supervise, does not exist in the property-file
    public static final String SUPERVISE_ACTIVEMQ_PREFIX          = "SUPERVISE_ACTIVEMQ_";
    public static final int    SUPERVISE_ACTIVEMQ_PREFIX_LENGTH = 19; // todo: SUPERVISE_ACTIVEMQ_PREFIX.length(); gives 0 ???
    public static final String SUPERVISE_ACTIVEMQ_URL             = "URL";
    public static final String SUPERVISE_ACTIVEMQ_QUEUE_DEPTH_AGE = "QUEUE_DEPTH_AGE";
    
    public static final String STOPFILE_NAME = "eRcptRtr.fel";
    
	private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

	private static Map<String, Object> resovledProperties = null;
	private static Map<Integer, Map> agreementsMap = null;

	static public Map<String, Object> getResovledProperties() {
    	if (resovledProperties == null) {
    		// FIXME. Inject the name of the property file!
    		resovledProperties = new PropertyUtil().getResovledProperties("ei-config");
    	}
		return resovledProperties;
    }
	
	static public Map<Integer, Map> getAgreementsMap() {
    	if (agreementsMap == null) {
    		agreementsMap = new HashMap<Integer, Map>();
    		
    		List<Map> agreementList = (List<Map>)getResovledProperties().get(PARTNER_LIST_NAME);
    		for (Map agmt : agreementList) {
    			agreementsMap.put((Integer)agmt.get(ID), agmt);
			}
    	}

    	return agreementsMap;
    }

	static public List<Map> getSuperviseFilesList() {
		return (List<Map>)getResovledProperties().get(SUPERVISE_FILES_LIST_NAME);
	}

	static public List<Map> getSupervisemqsList() {
		return (List<Map>)getResovledProperties().get(SUPERVISE_ACTIVEMQ_LIST_NAME);
	}

	private Map<String, Object> getResovledProperties(String... bundleNames) {
        RecursiveResourceBundle rb = new RecursiveResourceBundle(bundleNames);

        Set<String> keys = (Set)rb.getProperties().keySet();

        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : keys) {
            String value = rb.getString(key); // Here is where the recursive property resolve process takes place!
            put(map, key, value); // Here tmp maps of agreements, files and amq brokers to supervise are created
        }
        
//        // Create a list of the found partners, validate each partner entry and add it to the map
//        List<Map> partnerList = createListProperty(PARTNER_LIST_NAME, partnerRootMap, new PartnerValidator(), map);
//        
//        // Create a list of the found files to supervise, validate each entry and add it to the map
//        List<Map> superviseFilesList = createListProperty(SUPERVISE_FILES_LIST_NAME, superviseFilesRootMap, new SuperviseFilesValidator(), map);
//        
//        // Create a list of the found amq brokers to supervise, validate each entry and add it to the map
//        List<Map> superviseAmqsList = createListProperty(SUPERVISE_ACTIVEMQ_LIST_NAME, superviseAmqsRootMap, new SuperviseAmqsValidator(), map);
//        
//        log.info("Found {} properties, where {} are agreements, {} are supervise-files and {} are supervise-activemq brokers, ", 
//        		new Object[] {map.size(), partnerList.size(), superviseFilesList, superviseAmqsList});        
        return map;
    }

	private List<Map> createListProperty(final String listName,
			final Map<Integer, Map> rootMap, Validator validator,
			Map<String, Object> map) {
		// Create a list of the found entities, validate each partner entry and add it to the map
        List<Map> partnerList = new ArrayList<Map>();
        
        if (rootMap != null) {
			Set<Entry<Integer, Map>> partnerEntries = rootMap.entrySet();
	        for (Entry<Integer, Map> entry : partnerEntries) {
	            if (validator.validateProperties(map, entry)) {
	                partnerList.add(entry.getValue());
	            }
	        }
        }

        // Finally add the partner list
		map.put(listName, partnerList);
		return partnerList;
	}
    
    private void put(Map<String, Object> map, String key, String value) {
        
        // Is this a PARTNER(n)_(name) - parameter?
        if (key.startsWith(PARTNER_PREFIX)) {
            
            try {
                // Try to get the partner number and the PARTNER parameter name
                int numberStartPos = PARTNER_PREFIX_LENGTH;
                int numberEndpos = key.indexOf(SEPARATOR);
                String idStr = key.substring(numberStartPos, numberEndpos);
                int id = Integer.parseInt(idStr);
                
                String name = key.substring(numberEndpos + 1); // SKip the separator character
                

                Map<String, Object> partnerMap = getPartnerMap(id);
                partnerMap.put(name, value);
                
            } catch (NumberFormatException e) {
                // NO, something went wrong with the partner parsing, lets assume its a normal parameter.
                log.warn("Create partner parameter failed, handled as normal paramter, error: " + e.getMessage());
                e.printStackTrace();
                map.put(key, value);
            }

    
	    // Is this a SUPERVISOR_FILES_(n)_(name) - parameter?
	    } else if (key.startsWith(SUPERVISE_FILES_PREFIX)) {
            
            try {
                // Try to get the partner number and the SUPERVISE_FILES parameter name
                int numberStartPos = SUPERVISE_FILES_PREFIX_LENGTH;
                int numberEndpos = key.indexOf(SEPARATOR, numberStartPos);
                String idStr = key.substring(numberStartPos, numberEndpos);
                int id = Integer.parseInt(idStr);
                
                String name = key.substring(numberEndpos + 1); // SKip the separator character
                

                Map<String, Object> superviseFilesMap = getSuperviseFilesMap(id);
                superviseFilesMap.put(name, value);
                
                log.debug("Supervise file #" + id + " property: " + name + " = " + value);
                
            } catch (NumberFormatException e) {
                // NO, something went wrong with the parsing, lets assume its a normal parameter.
                log.warn("Create supervise files parameter failed, handled as normal paramter, error: " + e.getMessage());
                e.printStackTrace();
                map.put(key, value);
            }

    
	    // Is this a SUPERVISOR_ACTIVEMQ_(n)_(name) - parameter?
	    } else if (key.startsWith(SUPERVISE_ACTIVEMQ_PREFIX)) {
            
            try {
                // Try to get the partner number and the SUPERVISE_ACTIVEMQ parameter name
                int numberStartPos = SUPERVISE_ACTIVEMQ_PREFIX_LENGTH;
                int numberEndpos = key.indexOf(SEPARATOR);
                String idStr = key.substring(numberStartPos, numberEndpos);
                int id = Integer.parseInt(idStr);
                
                String name = key.substring(numberEndpos + 1); // SKip the separator character
                

                Map<String, Object> superviseAmqsMap = getSuperviseAmqsMap(id);
                superviseAmqsMap.put(name, value);
                
            } catch (NumberFormatException e) {
                // NO, something went wrong with the parsing, lets assume its a normal parameter.
                log.warn("Create supervise amqs parameter failed, handled as normal paramter, error: " + e.getMessage());
                e.printStackTrace();
                map.put(key, value);
            }

            
	    } else {
	        
	        // NO, its a normal parameter, just store it
	        map.put(key, value);
	    }

    
    }

    private Map<Integer, Map> partnerRootMap = null;

    private Map<String, Object> getPartnerMap(int id) {

        if (partnerRootMap == null) {
            partnerRootMap = new TreeMap<Integer, Map>();
        }
        
        Map<String, Object> partnerMap = (Map<String, Object>)partnerRootMap.get(id);
        if (partnerMap == null) {
        	// No map exists for this partner, create one and add a standard ID-property
        	// Also add the new partner-map to the root-map of all partners
            partnerMap = new HashMap<String, Object>();
            partnerMap.put(ID, id);
            partnerRootMap.put(id, partnerMap);
        }

        return partnerMap;
    }
    
    
    private Map<Integer, Map> superviseFilesRootMap = null;

    private Map<String, Object> getSuperviseFilesMap(int id) {

        if (superviseFilesRootMap == null) {
        	superviseFilesRootMap = new TreeMap<Integer, Map>();
        }
        
        Map<String, Object> superviseFilesMap = (Map<String, Object>)superviseFilesRootMap.get(id);
        if (superviseFilesMap == null) {
        	// No map exists for this superviseFiles - element, create one and add a standard ID-property
        	// Also add the new superviseFiles-map to the root-map of all files to supervise
        	superviseFilesMap = new HashMap<String, Object>();
        	superviseFilesMap.put(ID, id);
        	superviseFilesRootMap.put(id, superviseFilesMap);
        }

        return superviseFilesMap;
    }
    

    private Map<Integer, Map> superviseAmqsRootMap = null;

    private Map<String, Object> getSuperviseAmqsMap(int id) {

        if (superviseAmqsRootMap == null) {
        	superviseAmqsRootMap = new TreeMap<Integer, Map>();
        }
        
        Map<String, Object> superviseAmqsMap = (Map<String, Object>)superviseAmqsRootMap.get(id);
        if (superviseAmqsMap == null) {
        	// No map exists for this superviseAmqs - element, create one and add a standard ID-property
        	// Also add the new superviseAmqs-map to the root-map of all amq brokers to supervise
        	superviseAmqsMap = new HashMap<String, Object>();
        	superviseAmqsMap.put(ID, id);
        	superviseAmqsRootMap.put(id, superviseAmqsMap);
        }

        return superviseAmqsMap;
    }
    
//    private Set<String> uniquePartnerNames = new HashSet<String>();
	
}
