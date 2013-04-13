package se.skltp.ei.svc.entity.model.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Hash functions. <p>
 * 
 * @author Peter
 *
 */
public class Hash {

	private static MessageDigest digest;
	
	static {
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	//
	private Hash() {};
	
	
	/**
	 * Returns a SHA-2 hashed value
	 * 
	 * @param text array of string to hash
	 * @return 64 bit hash of the given string
	 */
	public static String shaHash(final String... text) {
		final StringBuffer buf = new StringBuffer();
		for (String s : text) {
			buf.append((s == null) ? "" : s);
		}
		final byte[] hash;		
		try {
			hash = digest.digest(buf.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		return UUID.nameUUIDFromBytes(hash).toString();
	}

}
