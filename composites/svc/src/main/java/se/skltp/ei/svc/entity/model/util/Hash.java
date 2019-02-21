/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 * <p>
 * This file is part of SKLTP.
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.entity.model.util;

import se.skltp.ei.svc.entity.model.Engagement;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash functions. <p>
 *
 * Use SHA-2 to create one-way hash keys and represent them as hex encoded strings. <p>
 *
 * For further information see http://en.wikipedia.org/wiki/SHA-2 <br>
 * All attempts to find collisions has failed (so far), typical performance 
 * for the actual implementation is at least 50.000 keys/s.
 *
 * @author Peter
 *
 */
public class Hash {

    /**
     * Makes sure multi-threaded access can be supported, i.e. MesssageDigest is not thread safe.
     */
    private static ThreadLocal<MessageDigest> digesters = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Hex digits.
     */
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    //
    private Hash() {
    }

    ;


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
        try {
            hash = digesters.get().digest(buf.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return asString(hash);
    }


    /**
     * Generates hash Id (obviously). Since its from time to time is useful to find Engagements that correspond to
     * the given engagement but with another owner, this method enables generating id suitable for this.
     * @param engagement source of hash id
     * @param pOwner may differ from the engagement owner
     * @return
     */
    public static String generateHashId(Engagement engagement, String pOwner) {

        String hash = Hash.sha2(engagement.getRegisteredResidentIdentification(),
                engagement.getServiceDomain(),
                engagement.getCategorization(),
                engagement.getLogicalAddress(),
                engagement.getBusinessObjectInstanceIdentifier(),
                engagement.getSourceSystem(),
                engagement.getDataController(),
                pOwner,
                engagement.getClinicalProcessInterestId());
        return hash;
    }

    public static String generateHashId(Engagement engagement) {
        return generateHashId(engagement, engagement.getOwner());
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
            buf.append(digits[(b & 0xf0) >> 4]).append(digits[b & 0x0f]);
        }
        return buf.toString();
    }

}
