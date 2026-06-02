/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.service.util;

public class EIUtils {
	
  public static boolean isEmpty(String s) {
	  return (s == null || s.trim().length()==0);
  }
  
  public static boolean isTrimmed(String s) {
	  return s.trim().length() == s.length();
  }
  
}
