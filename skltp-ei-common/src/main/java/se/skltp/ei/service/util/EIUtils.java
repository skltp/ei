package se.skltp.ei.service.util;

public class EIUtils {
	
  public static boolean isEmpty(String s) {
	  return (s == null || s.trim().length()==0);
  }
  
  public static boolean isTrimmed(String s) {
	  return s.trim().length() == s.length();
  }
  
}
