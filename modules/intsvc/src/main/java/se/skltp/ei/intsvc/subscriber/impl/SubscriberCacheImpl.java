package se.skltp.ei.intsvc.subscriber.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.intsvc.subscriber.api.SubscriberCache;

public class SubscriberCacheImpl implements SubscriberCache {

	private static final Logger log = LoggerFactory.getLogger(SubscriberCacheImpl.class);
	
	private List<Subscriber> subscribers;
	private boolean initialized;

	/**
	 * Path to filename for the cache file
	 */
	private String filePath;

    // cache
    @XmlRootElement
    static class PersistentCache implements Serializable {
    	private static final long serialVersionUID = 1L;

    	@XmlElement
    	private List<Subscriber> subscribers;
    	
    }
    
    private static final JaxbUtil JAXB = new JaxbUtil(PersistentCache.class);
	

	public SubscriberCacheImpl() {
		reset();
	}
	

    /**
     * Set the filepath to where cache file should be stored
     * 
     * @param path path including filename for the cache file
     */
    public void setFilePath(String path) {
    	filePath = path;
    }


	@Override
	public List<Subscriber> getSubscribers() {
		// TODO. Add lazy read of subscribers here?
		// TODO. Save to local file if successful read
		// TODO. Log Warn and load from local file if lazy read fails
		// TODO. Throw and log error if load from both TAK and local file fails?
		return subscribers;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void reset() {
		subscribers = new ArrayList<Subscriber>();
		initialized = false;
	}

	@Override
	public void initialize(List<Subscriber> subscribers) {
		this.subscribers = subscribers;
		initialized = true;
	}
	
	/**
	 * Save the contents of subscribers to file
	 */
	@Override
	public void saveToLocalCopy() {

		PersistentCache pc = new PersistentCache();
		pc.subscribers = subscribers;

		OutputStream os = null;
		
		try {
			
			log.info("Starting to save EI subscribers to local cache: " + filePath);
			
			File file = new File(filePath);
			os = new FileOutputStream(file);
		
			os.write(JAXB.marshal(pc).getBytes("UTF-8"));
			
			log.info("Succesfully saved EI subscribers to local cache: " + filePath);

		} catch (Exception e) {
			log.error("Failed to save EI subscribers to local cache: " + filePath);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {}	
			}
		}
		
	}
	
	/**
	 * Load subscribers from file into subscribers
	 */
	@Override
	public void restoreFromLocalCopy() {
		
        PersistentCache pc = null;
        InputStream is = null;
        final File file = new File(filePath);
        
        try {
        	
        	log.info("Starting to load EI subscribers from local cache: ", filePath);
        	
        	if (file.exists()) {
        		
        		is = new FileInputStream(filePath);
        		pc = (PersistentCache) JAXB.unmarshal(is);
        		
        		if (pc.subscribers != null) {
        			
        			this.subscribers = pc.subscribers;
        			log.info("Succesfully loaded EI subscribers to local cache: " + filePath);
        			
        		} else {
        			log.warn("There is no EI subscribers available in local cache: " + filePath);
        		}
        		
        		
        	} else {
        		log.error("Failed to load EI subscribers from local cache, no such file:" + filePath);
        	}
        	
        } catch (Exception e ) {

        	log.error("Failed to load EI subscribers from local cache: " + e.getMessage());
        	
        	if (is != null) {
        		file.delete();
        	}
			
        } finally {
        	if (is != null) {
        		try { is.close();} catch (IOException e) {}
        	}
        }
		
	}
}
