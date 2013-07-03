package se.skltp.ei.intsvc.getupdates.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Henrik Rostam
 **/

public class PropertyResolver {

    private static Map<String, Object> properties = new HashMap<String, Object>();

    private PropertyResolver() {

    }

    public void setProperties(Map<String, Object> properties) {
        PropertyResolver.properties = properties;
    }

    public static String get(String name) {
        return String.valueOf(properties.get(name));
    }
}

