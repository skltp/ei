package se.skltp.ei.intsvc;

public interface EiConstants {

	/*
	 * Http header x-vp-sender-id, for EI to use when acting consumer towards VP. 
	 * Http heaeder x-vp-instance-id, for EI to use when acting consumer towards VP.
	 * 
	 * These two headers are dependent on each other in a way that when using x-vp-sender-id
	 * against VP, VP will check for a valid x-vp-instance-id.
	 */
	public static final String X_VP_SENDER_ID = "x-vp-sender-id";
	public static final String X_VP_INSTANCE_ID = "x-vp-instance-id";
	
	/**
	 * The RIVTA 2.1 (optional) header used to propagate original consumer id,
	 * used in EI for logging message metadata.   
	 */
	public static final String X_RIVTA_ORIGINAL_CONSUMER_ID = "x-rivta-original-serviceconsumer-hsaid";
	/**
	 * The SKLTP correlation id header.
	 */
	public static final String X_SKLTP_CORRELATION_ID = "x-skltp-correlation-id";
	/**
	 * Property name for propagating X_RIVTA_ORIGINAL_CONSUMER_ID within EI over JMS queues,
	 * the dashes in X_RIVTA_ORIGINAL_CONSUMER_ID is not allowed in JMS properties and will
	 * be replaced by Mule in runtime.
	 */
	public static final String EI_ORIGINAL_CONSUMER_ID = "originalServiceconsumerHsaid";
	/**
	 * Property name for logging metadata.
	 */	
	public static final String EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE = "nrRecords";
	/**
	 * Property name for logging metadata.
	 */
	public static final String EI_LOG_IS_UPDATE_ROUTED_VIA_COLLECT = "isViaCollect";
	/**
	 * Property name for logging metadata.
	 */
	public static final String EI_LOG_MESSAGE_TYPE = "msgType";
	public static final String EI_LOG_MESSAGE_TYPE_UPDATE = "update";
	public static final String EI_LOG_MESSAGE_TYPE_PROCESS_NOTIFICATION = "procnotif";
	/**
	 * Property name for logging metadata from update collect.
	 */	
	public static final String EI_LOG_UPDATE_COLLECT_NR_MESSAGES = "collectNrMsgs";
	public static final String EI_LOG_UPDATE_COLLECT_NR_RECORDS = "collectNrRecords";
	public static final String EI_LOG_UPDATE_COLLECT_BUFFER_AGE_MS = "collectBufAgeMs";	
	
}
