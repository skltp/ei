# Helm Values Reference

This page documents every configurable value in `helm/values.yaml` for the EI (Engagemangsindex) Helm chart.

---

## repository

| Key          | Description                                                                                                                                                                           |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `repository` | Container image registry prefix (e.g. `docker.drift.inera.se/ntjp/`). Prepended to the image name when constructing the full image reference. **Must be overridden per environment.** |

---

## container

Container image settings for the backend and frontend deployments.

| Key                                   | Description                                                                                              |
|---------------------------------------|----------------------------------------------------------------------------------------------------------|
| `container.backend.image.tag`         | Image tag / version for the EI backend container. Defaults to the Helm chart `appVersion` when not set.  |
| `container.backend.image.pullPolicy`  | Kubernetes image pull policy for the backend (`Always`, `IfNotPresent`, `Never`).                        |
| `container.frontend.image.tag`        | Image tag / version for the EI frontend container. Defaults to the Helm chart `appVersion` when not set. |
| `container.frontend.image.pullPolicy` | Kubernetes image pull policy for the frontend (`Always`, `IfNotPresent`, `Never`).                       |

---

## vip

Backwards-compatible Kubernetes Service for environments that reference EI by a legacy service name.

| Key        | Description                                                   |
|------------|---------------------------------------------------------------|
| `vip.name` | Name of the backwards-compatible Kubernetes Service resource. |

---

## queues

| Key              | Description                                                                                                     |
|------------------|-----------------------------------------------------------------------------------------------------------------|
| `queues.prefix`  | Common prefix applied to all JMS queue names. Override per environment to namespace queues (e.g. `ENV.EI.`).    |

---

## deployments

Deployment settings for the backend and frontend pods.

| Key                                    | Description                                                                                  |
|----------------------------------------|----------------------------------------------------------------------------------------------|
| `deployments.backend.replicaCount`     | Number of pod replicas for the EI backend.                                                   |
| `deployments.backend.elasticGrokFilter`| Value injected as a label for Elastic log pipeline grok-filter matching (backend).           |
| `deployments.frontend.replicaCount`    | Number of pod replicas for the EI frontend.                                                  |
| `deployments.frontend.elasticGrokFilter`| Value injected as a label for Elastic log pipeline grok-filter matching (frontend).         |

---

## backend_resources

Kubernetes resource requests and limits for the backend container.

| Key                              | Description                                                |
|----------------------------------|------------------------------------------------------------|
| `backend_resources.limits.memory`   | Maximum memory the backend container may use.           |
| `backend_resources.requests.cpu`    | CPU request for scheduling the backend pod.             |
| `backend_resources.requests.memory` | Memory request for scheduling the backend pod.          |

---

## frontend_resources

Kubernetes resource requests and limits for the frontend container.

| Key                               | Description                                                |
|-----------------------------------|------------------------------------------------------------|
| `frontend_resources.limits.memory`   | Maximum memory the frontend container may use.          |
| `frontend_resources.requests.cpu`    | CPU request for scheduling the frontend pod.            |
| `frontend_resources.requests.memory` | Memory request for scheduling the frontend pod.         |

---

## paths

File-system paths inside the EI containers.

| Key                    | Description                                                           |
|------------------------|-----------------------------------------------------------------------|
| `paths.loggingConfig`  | Path to the Log4j2 configuration file inside the container.           |

---

## ei

EI application identity settings.

| Key         | Description                                                                                                |
|-------------|------------------------------------------------------------------------------------------------------------|
| `ei.hsaId`  | EI's own HSA identity. Used as sender identity and for circular-routing prevention. Maps to `EI_HSA_ID`.   |

---

## vp

Virtualization Platform (VP) settings.

| Key         | Description                                                                                                                    |
|-------------|--------------------------------------------------------------------------------------------------------------------------------|
| `vp.hsaId`  | VP's HSA identity. Used as sender HSA-ID when EI calls VP outbound services. Maps to `VP_HSA_ID`.                              |

---

## skltp

| Key                | Description                                                                                                               |
|--------------------|---------------------------------------------------------------------------------------------------------------------------|
| `skltp.instanceId` | Unique identifier for this SKLTP instance. Used for correlation and instance identification. Maps to `SKLTP_INSTANCE_ID`. |

---

## activemq

Shared ActiveMQ broker settings used by both backend and frontend.

| Key                                     | Description                                                                                                                          |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `activemq.broker.url`                   | ActiveMQ broker connection URL. Supports failover syntax. Maps to `ACTIVEMQ_BROKER_URL`.                                             |
| `activemq.broker.maximumRedeliveries`   | Maximum number of redelivery attempts before a message is sent to the DLQ. Maps to `ACTIVEMQ_BROKER_MAXIMUM_REDELIVERIES`.           |
| `activemq.broker.redeliveryDelay`       | Delay in milliseconds between redelivery attempts. Maps to `ACTIVEMQ_BROKER_REDELIVERY_DELAY`.                                       |
| `activemq.broker.useExponentialBackoff` | Whether to use exponential back-off between redelivery attempts (`true`/`false`). Maps to `ACTIVEMQ_BROKER_USE_EXPONENTIAL_BACKOFF`. |

---

## management

Spring Boot Actuator / management endpoint configuration.

| Key                                      | Description                                                                      |
|------------------------------------------|----------------------------------------------------------------------------------|
| `management.endpointsWebExposureInclude` | List of Actuator endpoint IDs to expose over HTTP (e.g. `health`, `prometheus`). |
| `management.endpointPrometheusEnabled`   | Enable the Prometheus metrics scrape endpoint (`true`/`false`).                  |

---

## backend

Backend-specific application configuration.

### backend.server

| Key                   | Description                                                                          |
|-----------------------|--------------------------------------------------------------------------------------|
| `backend.server.port` | Internal server port for the backend Spring Boot application. Maps to `SERVER_PORT`. |

### backend.findContent

| Key                                 | Description                                                                                        |
|-------------------------------------|----------------------------------------------------------------------------------------------------|
| `backend.findContent.webserviceUrl` | Listener URL for the FindContent SOAP endpoint (port 8082). Maps to `FIND_CONTENT_WEBSERVICE_URL`. |

### backend.getLogicalAddresses

| Key                                              | Description                                                                                  |
|--------------------------------------------------|----------------------------------------------------------------------------------------------|
| `backend.getLogicalAddresses.serviceEndpointUrl` | URL of the VP `GetLogicalAddresseesByServiceContract` endpoint used to discover subscribers. |
| `backend.getLogicalAddresses.connectTimeoutMs`   | TCP connect timeout in milliseconds when calling GetLogicalAddresses.                        |
| `backend.getLogicalAddresses.requestTimeoutMs`   | HTTP request (read) timeout in milliseconds when calling GetLogicalAddresses.                |

### backend.processNotification

| Key                                              | Description                                                                              |
|--------------------------------------------------|------------------------------------------------------------------------------------------|
| `backend.processNotification.serviceEndpointUrl` | URL of the VP `ProcessNotification` endpoint used for outbound subscriber notifications. |

### backend.subscriberCache

| Key                                         | Description                                                                                           |
|---------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `backend.subscriberCache.fileName`          | File path for the persisted local subscriber cache. Used as fallback when the service is unavailable. |
| `backend.subscriberCache.timeToLiveSeconds` | Time-to-live in seconds for subscriber cache entries before automatic refresh.                        |
| `backend.subscriberCache.resetUrl`          | Internal listener URL for the subscriber cache reset endpoint.                                        |
| `backend.subscriberCache.statusUrl`         | Internal listener URL for the subscriber cache status endpoint (returns JSON).                        |

### backend.statusUrl

| Key                 | Description                                                   |
|---------------------|---------------------------------------------------------------|
| `backend.statusUrl` | Internal listener URL for the backend health/status endpoint. |

### backend.writelock

Write-lock endpoints for zero-downtime database migration support.

| Key                            | Description                                                     |
|--------------------------------|-----------------------------------------------------------------|
| `backend.writelock.enableUrl`  | URL to enable the write lock (suspends process/collect routes). |
| `backend.writelock.disableUrl` | URL to disable the write lock (resumes process/collect routes). |
| `backend.writelock.statusUrl`  | URL to check current write-lock status.                         |

### backend.datasource

Database connection and JPA/Hibernate settings.

| Key                                       | Description                                                                                      |
|-------------------------------------------|--------------------------------------------------------------------------------------------------|
| `backend.datasource.url`                  | JDBC connection URL for the engagement index database. **Override per environment.**             |
| `backend.datasource.driverClassName`      | Fully-qualified JDBC driver class name (e.g. `com.mysql.cj.jdbc.Driver`).                        |
| `backend.datasource.jpa.databasePlatform` | Hibernate dialect class for the target database (e.g. `org.hibernate.dialect.MySQL8Dialect`).    |
| `backend.datasource.jpa.hibernateDdlAuto` | Hibernate DDL auto mode (`none`, `validate`, `update`, `create-drop`). Use `none` in production. |

### backend.activemq

Backend-specific ActiveMQ redelivery settings for notification queues.

| Key                                                          | Description                                                                                        |
|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `backend.activemq.broker.notificationMaximumRedeliveries`    | Maximum redelivery attempts for notification queue messages before DLQ.                            |
| `backend.activemq.broker.notificationRedeliveryDelay`        | Initial delay in milliseconds between notification redelivery attempts.                            |
| `backend.activemq.broker.notificationUseExponentialBackoff`  | Whether to use exponential back-off for notification redeliveries (`true`/`false`).                |
| `backend.activemq.broker.notificationBackoffMultiplier`      | Multiplier applied to the delay on each successive notification redelivery attempt.                |
| `backend.activemq.broker.notificationMaximumRedeliveryDelay` | Upper bound in milliseconds for the notification redelivery delay when using exponential back-off. |

---

## frontend

Frontend-specific application configuration.

### frontend.server

| Key                    | Description                                                                           |
|------------------------|---------------------------------------------------------------------------------------|
| `frontend.server.port` | Internal server port for the frontend Spring Boot application. Maps to `SERVER_PORT`. |

### frontend.update

| Key                             | Description                                                                             |
|---------------------------------|-----------------------------------------------------------------------------------------|
| `frontend.update.webserviceUrl` | Listener URL for the Update SOAP endpoint (port 8081). Maps to `UPDATE_WEBSERVICE_URL`. |

### frontend.processNotification

| Key                                          | Description                                                                                                                |
|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `frontend.processNotification.webserviceUrl` | Listener URL for the inbound ProcessNotification SOAP endpoint (port 8081). Maps to `PROCESS_NOTIFICATION_WEBSERVICE_URL`. |

### frontend.statusUrl

| Key                  | Description                                                    |
|----------------------|----------------------------------------------------------------|
| `frontend.statusUrl` | Internal listener URL for the frontend health/status endpoint. |

### frontend.updateNotification

| Key                                               | Description                                                                                                                                                   |
|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `frontend.updateNotification.notAllowedHsaidList` | Comma-separated list of HSA-IDs whose updates are dropped to prevent circular routing between federated EI instances. Must include `ei.hsaId` and `vp.hsaId`. |

---

## environment

ConfigMap and Secret references injected into the containers as environment variables.

### environment.backend

| Key                                        | Description                                                                                           |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `environment.backend._default_config_maps` | List of ConfigMap names whose keys are injected as environment variables into the backend by default. |
| `environment.backend.config_maps`          | Additional ConfigMap names to inject into the backend. Override per environment.                      |
| `environment.backend.secrets`              | Kubernetes Secret names whose keys are injected as environment variables into the backend.            |

### environment.frontend

| Key                                         | Description                                                                                            |
|---------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `environment.frontend._default_config_maps` | List of ConfigMap names whose keys are injected as environment variables into the frontend by default. |
| `environment.frontend.config_maps`          | Additional ConfigMap names to inject into the frontend. Override per environment.                      |
| `environment.frontend.secrets`              | Kubernetes Secret names whose keys are injected as environment variables into the frontend.            |

---

## log4j

Log4j2 logger configuration rendered into a ConfigMap-based `log4j2.xml`.

| Key                     | Description                                                                                                                               |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `log4j.loggers`         | List of logger entries. Each entry has a `name` (logger name / package) and a `level` (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `OFF`). |
| `log4j.rootLoggerLevel` | Log level for the root logger. All loggers not explicitly listed inherit this level.                                                      |

---

## probes_backend

Kubernetes health probes for the EI backend container.

### probes_backend.startupProbe

| Key                                               | Description                                                                |
|---------------------------------------------------|----------------------------------------------------------------------------|
| `probes_backend.startupProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                          |
| `probes_backend.startupProbe.httpGet.port`        | Named or numeric port to probe.                                            |
| `probes_backend.startupProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                       |
| `probes_backend.startupProbe.initialDelaySeconds` | Seconds to wait before the first probe after container start.              |
| `probes_backend.startupProbe.periodSeconds`       | Seconds between probe attempts.                                            |
| `probes_backend.startupProbe.timeoutSeconds`      | Seconds before a single probe attempt times out.                           |
| `probes_backend.startupProbe.successThreshold`    | Number of consecutive successes required to mark the container as started. |
| `probes_backend.startupProbe.failureThreshold`    | Number of consecutive failures before the container is restarted.          |

### probes_backend.livenessProbe

| Key                                                | Description                                                            |
|----------------------------------------------------|------------------------------------------------------------------------|
| `probes_backend.livenessProbe.httpGet.path`        | HTTP path to probe (Actuator liveness endpoint).                       |
| `probes_backend.livenessProbe.httpGet.port`        | Named or numeric port to probe.                                        |
| `probes_backend.livenessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                   |
| `probes_backend.livenessProbe.initialDelaySeconds` | Seconds to wait before the first liveness probe.                       |
| `probes_backend.livenessProbe.periodSeconds`       | Seconds between liveness probes.                                       |
| `probes_backend.livenessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                              |
| `probes_backend.livenessProbe.failureThreshold`    | Consecutive failures before the container is killed and restarted.     |
| `probes_backend.livenessProbe.successThreshold`    | Consecutive successes to clear a failed state.                         |

### probes_backend.readinessProbe

| Key                                                 | Description                                                            |
|-----------------------------------------------------|------------------------------------------------------------------------|
| `probes_backend.readinessProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                      |
| `probes_backend.readinessProbe.httpGet.port`        | Named or numeric port to probe.                                        |
| `probes_backend.readinessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                   |
| `probes_backend.readinessProbe.initialDelaySeconds` | Seconds to wait before the first readiness probe.                      |
| `probes_backend.readinessProbe.periodSeconds`       | Seconds between readiness probes.                                      |
| `probes_backend.readinessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                              |
| `probes_backend.readinessProbe.failureThreshold`    | Consecutive failures before the pod is removed from service endpoints. |
| `probes_backend.readinessProbe.successThreshold`    | Consecutive successes to mark the pod as ready.                        |

---

## probes_frontend

Kubernetes health probes for the EI frontend container.

### probes_frontend.startupProbe

| Key                                                | Description                                                                |
|----------------------------------------------------|----------------------------------------------------------------------------|
| `probes_frontend.startupProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                          |
| `probes_frontend.startupProbe.httpGet.port`        | Named or numeric port to probe.                                            |
| `probes_frontend.startupProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                       |
| `probes_frontend.startupProbe.initialDelaySeconds` | Seconds to wait before the first probe after container start.              |
| `probes_frontend.startupProbe.periodSeconds`       | Seconds between probe attempts.                                            |
| `probes_frontend.startupProbe.timeoutSeconds`      | Seconds before a single probe attempt times out.                           |
| `probes_frontend.startupProbe.successThreshold`    | Number of consecutive successes required to mark the container as started. |
| `probes_frontend.startupProbe.failureThreshold`    | Number of consecutive failures before the container is restarted.          |

### probes_frontend.livenessProbe

| Key                                                 | Description                                                        |
|-----------------------------------------------------|--------------------------------------------------------------------|
| `probes_frontend.livenessProbe.httpGet.path`        | HTTP path to probe (Actuator liveness endpoint).                   |
| `probes_frontend.livenessProbe.httpGet.port`        | Named or numeric port to probe.                                    |
| `probes_frontend.livenessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                               |
| `probes_frontend.livenessProbe.initialDelaySeconds` | Seconds to wait before the first liveness probe.                   |
| `probes_frontend.livenessProbe.periodSeconds`       | Seconds between liveness probes.                                   |
| `probes_frontend.livenessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                          |
| `probes_frontend.livenessProbe.failureThreshold`    | Consecutive failures before the container is killed and restarted. |
| `probes_frontend.livenessProbe.successThreshold`    | Consecutive successes to clear a failed state.                     |

### probes_frontend.readinessProbe

| Key                                                  | Description                                                            |
|------------------------------------------------------|------------------------------------------------------------------------|
| `probes_frontend.readinessProbe.httpGet.path`        | HTTP path to probe (Actuator readiness endpoint).                      |
| `probes_frontend.readinessProbe.httpGet.port`        | Named or numeric port to probe.                                        |
| `probes_frontend.readinessProbe.httpGet.scheme`      | Protocol scheme (`HTTP` or `HTTPS`).                                   |
| `probes_frontend.readinessProbe.initialDelaySeconds` | Seconds to wait before the first readiness probe.                      |
| `probes_frontend.readinessProbe.periodSeconds`       | Seconds between readiness probes.                                      |
| `probes_frontend.readinessProbe.timeoutSeconds`      | Seconds before a probe attempt times out.                              |
| `probes_frontend.readinessProbe.failureThreshold`    | Consecutive failures before the pod is removed from service endpoints. |
| `probes_frontend.readinessProbe.successThreshold`    | Consecutive successes to mark the pod as ready.                        |

---

## See Also

- [Configuration](config.md) — Application-level property documentation.
