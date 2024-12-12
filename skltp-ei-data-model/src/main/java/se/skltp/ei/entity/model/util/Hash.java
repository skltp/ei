/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 * <p>
 * This file is part of SKLTP.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.entity.model.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import se.skltp.ei.entity.model.BusinessKey;

/**
 * Hash functions. <p>
 *
 * Use SHA-2 to create one-way hash keys and represent them as hex encoded strings. <p>
 *
 * For further information see http://en.wikipedia.org/wiki/SHA-2 <br>
 * All attempts to find collisions has failed (so far), typical performance 
 * for the actual implementation is at least 50.000 keys/s.
 */
public class Hash {

  /**
   * Makes sure multithreaded access can be supported, i.e. MessageDigest is not thread safe.
   */
  private static final ThreadLocal<MessageDigest> digesters = ThreadLocal.withInitial(() -> {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  });

  private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


  private Hash() {
    // Static utility
  }

  public void unload() {
    digesters.remove();
  }

  /**
   * Returns a SHA-2 hashed value
   *
   * @param text array of string to hash
   * @return 64 bytes hash of the given string
   */
  public static String sha2(final String... text) {
    final StringBuilder buf = new StringBuilder(1024);
    for (String s : text) {
      buf.append((s == null) ? "" : s);
    }
    final byte[] hash;
    hash = digesters.get().digest(buf.toString().getBytes(StandardCharsets.UTF_8));

    return asString(hash);
  }


  /**
   * Generates hash Id (obviously). Since its from time to time is useful to find Engagements that correspond to
   * the given engagement but with another owner, this method enables generating id suitable for this.
   * @param businessKey source of hash id
   * @param pOwner may differ from the engagement owner
   * @return
   */
  public static String generateHashId(BusinessKey businessKey, String pOwner) {
    return Hash.sha2(businessKey.getRegisteredResidentIdentification(),
        businessKey.getServiceDomain(),
        businessKey.getCategorization(),
        businessKey.getLogicalAddress(),
        businessKey.getBusinessObjectInstanceIdentifier(),
        businessKey.getSourceSystem(),
        businessKey.getDataController(),
        pOwner,
        businessKey.getClinicalProcessInterestId());
  }

  public static String generateHashId(BusinessKey businessKey) {
    return generateHashId(businessKey, businessKey.getOwner());
  }

  /**
   * Returns a string representation of a byte array.
   *
   * @param bytes the bytes.
   * @return the string representation.
   */
  private static String asString(final byte[] bytes) {
    final StringBuilder buf = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      buf.append(HEX_DIGITS[(b & 0xf0) >> 4]).append(HEX_DIGITS[b & 0x0f]);
    }
    return buf.toString();
  }

}
