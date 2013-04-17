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