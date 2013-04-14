package se.skltp.ei.svc.entity.model.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash functions. <p>
 * 
 * Use SHA-2 to create one-way hash keys and represent them has hex encoded strings.
 * 
 * @author Peter
 *
 */
public class Hash {

	/**
	 * Digest.
	 */
	private static MessageDigest messageDigest;
	/**
	 * Hex digits.
	 */
	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	static {
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
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
	public static String sha2(final String... text) {
		final StringBuffer buf = new StringBuffer();
		for (String s : text) {
			buf.append((s == null) ? "" : s);
		}
		final byte[] hash;		
		try {
			hash = messageDigest.digest(buf.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		return asString(hash);
	}
	
	
	/**
	 * Returns a string representation of a byte array.
	 * 
	 * @param bytes the bytes.
	 * @return the string representation.
	 */
	private static String asString(final byte[] bytes) {
		final StringBuilder buf = new StringBuilder();		
		for (byte b : bytes) {
			buf.append(digits[(b & 0xf0) >> 4]);
			buf.append(digits[b & 0x0f]);
		}
		return buf.toString();
	}

}
