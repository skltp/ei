/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.writelock;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WriteLockStatusProcessor implements Processor {

    @Autowired
    private WriteLockService writeLockService;

    @Autowired
    private CamelContext camelContext;

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("writeLockEnabled", writeLockService.isEnabled());

        Map<String, String> routes = new LinkedHashMap<>();
        for (String routeId : new String[]{
                writeLockService.getProcessRouteId(),
                writeLockService.getCollectRouteId()}) {
            ServiceStatus s = camelContext.getRouteController().getRouteStatus(routeId);
            routes.put(routeId, s != null ? s.name() : "Unknown");
        }
        status.put("routes", routes);

        ObjectMapper mapper = new ObjectMapper();
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(new DefaultIndenter().withLinefeed(System.lineSeparator()));
        exchange.getIn().setBody(mapper.writer(printer).writeValueAsString(status));
    }
}
