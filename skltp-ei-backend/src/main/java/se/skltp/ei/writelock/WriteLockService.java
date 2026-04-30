package se.skltp.ei.writelock;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Manages a write lock that suppresses all database writes by suspending the
 * Camel routes that consume from the JMS queues leading to persistence.
 *
 * Suspending a transacted JMS consumer route causes Camel to finish the current
 * in-flight transaction, then stop polling. Messages remain durably buffered in
 * ActiveMQ. Resuming the route restores normal processing.
 *
 * Affected route IDs (configurable via properties, with defaults matching the
 * .id() values in their respective RouteBuilder classes):
 *   - ei.route.id.process  → EiBackendUpdateRoute   → writes to DB
 *   - ei.route.id.collect  → EiBackendCollectRoute  → feeds the process queue
 *
 * NOT affected: FindContent, frontend SOAP, dynamic notification routes.
 */
@Log4j2
@Service
public class WriteLockService {

    @Value("${ei.route.id.process:backend-process-route}")
    private String processRouteId;

    @Value("${ei.route.id.collect:backend-collection-route}")
    private String collectRouteId;

    @Autowired
    private CamelContext camelContext;

    private volatile boolean writeLockEnabled = false;

    /**
     * Enables the write lock by suspending the collect and process routes.
     * Idempotent: calling while already enabled is a no-op.
     */
    public synchronized void enable() throws Exception {
        if (writeLockEnabled) {
            log.warn("Write lock is already enabled – no action taken");
            return;
        }
        log.warn("Enabling write lock: suspending '{}' and '{}'", collectRouteId, processRouteId);
        camelContext.getRouteController().suspendRoute(collectRouteId);
        camelContext.getRouteController().suspendRoute(processRouteId);
        writeLockEnabled = true;
        log.warn("Write lock ENABLED – database writes suppressed; messages buffered in ActiveMQ");
    }

    /**
     * Disables the write lock by resuming the collect and process routes.
     * Idempotent: calling while not enabled is a no-op.
     */
    public synchronized void disable() throws Exception {
        if (!writeLockEnabled) {
            log.warn("Write lock is already disabled – no action taken");
            return;
        }
        log.warn("Disabling write lock: resuming '{}' and '{}'", collectRouteId, processRouteId);
        camelContext.getRouteController().resumeRoute(collectRouteId);
        camelContext.getRouteController().resumeRoute(processRouteId);
        writeLockEnabled = false;
        log.warn("Write lock DISABLED – database writes resumed");
    }

    public boolean isEnabled() {
        return writeLockEnabled;
    }

    public String getProcessRouteId() {
        return processRouteId;
    }

    public String getCollectRouteId() {
        return collectRouteId;
    }
}
