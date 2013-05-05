/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.dynamicFlows;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.MiscUtil;

public class CreateDynamicFlows  {

    private static final Logger log = LoggerFactory.getLogger(CreateDynamicFlows.class);
    private final Map<String, Object> propertyMap;
    
    public CreateDynamicFlows(List<String> logicalAdresses) {
        propertyMap = PropertyUtil.getResovledProperties();
        propertyMap.put("LOGICAL_ADDRESSES", logicalAdresses);
    }
    
    public List<String> getContextConfiguration() {
        ArrayList<String> configFiles = new ArrayList<String>();

        int flowNo = 0;
        Object configFile = null;
        log.info("Loading static flows...");
        do {
            String param = "STATIC_FLOW_" + ++flowNo;
            configFile = propertyMap.get(param);
            if (configFile == null) {
                flowNo--;
                log.info("Stop loading static configurations, loaded {} config files", flowNo);

            } else {
                // Load the static config
                log.info("Load static config #{} from config file {}", flowNo, configFile);
                String sourceCode = loadConfigFile(configFile.toString());
                log.debug("Loaded static configuration #{}, length {}", flowNo, sourceCode.length());

                configFiles.add(sourceCode);
            }
            
        } while (configFile != null);
        
        flowNo = 0;
        Object templateFile = null;
        log.info("Loading dynamic flows...");
        do {
            String param = "DYNAMIC_FLOW_" + ++flowNo;
            templateFile = propertyMap.get(param);
            if (templateFile == null) {
                flowNo--;
                log.info("Stop loading template files for dynamic flows, loaded {} config files", flowNo);

            } else {
                // Generate the actual source code
                log.info("Generate dynamic flow #{} from template file {}", flowNo, templateFile);
                SourceCodeGenerator scg = new SourceCodeGenerator(templateFile.toString());
                String sourceCode = scg.generateContent(propertyMap);
                log.debug("Generated dynamic flow #{}, length {}", flowNo, sourceCode.length());

                configFiles.add(sourceCode);
            }
            
        } while (templateFile != null);
        
        return configFiles;
    }
    
    private String loadConfigFile(String configFile) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(configFile);
        String config = MiscUtil.convertStreamToString(is);
        
        return config;
    }

}