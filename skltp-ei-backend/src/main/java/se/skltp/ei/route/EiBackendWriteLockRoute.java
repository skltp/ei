package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.ei.writelock.WriteLockService;
import se.skltp.ei.writelock.WriteLockStatusProcessor;

/**
 * Management HTTP routes for the write lock feature.
 *
 * All three endpoints are served on the internal management port (8083 in
 * production), not the public SOAP port.
 *
 * GET {{writelock.enable.url}}   → enable write lock, returns plain text
 * GET {{writelock.disable.url}}  → disable write lock, returns plain text
 * GET {{writelock.status.url}}   → JSON status (WriteLockStatusProcessor)
 */
@Component
public class EiBackendWriteLockRoute extends RouteBuilder {

    @Autowired
    private WriteLockService writeLockService;

    @Autowired
    private WriteLockStatusProcessor writeLockStatusProcessor;

    @Override
    public void configure() throws Exception {

        from("jetty://{{writelock.enable.url}}")
                .routeId("backend-writelock-enable-route")
                .process(ex -> writeLockService.enable())
                .setBody(simple("Write lock enabled"));

        from("jetty://{{writelock.disable.url}}")
                .routeId("backend-writelock-disable-route")
                .process(ex -> writeLockService.disable())
                .setBody(simple("Write lock disabled"));

        from("jetty://{{writelock.status.url}}")
                .routeId("backend-writelock-status-route")
                .process(writeLockStatusProcessor)
                .setHeader("Content-type", simple("application/json"));
    }
}
