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
	
}
