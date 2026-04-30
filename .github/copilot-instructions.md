# GitHub Copilot Instructions – SKLTP Engagemangsindex (EI)

## Project Overview

This is the **SKLTP Engagement Index (Engagemangsindex)** – a national Swedish healthcare infrastructure service that acts as a patient index. It enables national and regional e-services to locate patient information across care providers and regional boundaries. EI holds no source data; it mirrors index attributes from source systems.

- **Stack**: Java 17, Spring Boot 3.3.7, Apache Camel 4.8.3 LTS, Apache CXF 4.1.0, ActiveMQ 6.1.4, Spring Data JPA / Hibernate, EhCache 3.10.8
- **Servlet container**: Jetty (not Tomcat)
- **Build**: Maven multi-module (8 modules)
- **Deployment**: Docker (Eclipse Temurin 17 Alpine) + Kubernetes via Helm charts
- **Database**: MySQL 8 in production, H2 in-memory for local/test

---

## Maven Module Layout

| Module | Role |
|---|---|
| `skltp-ei-schemas` | WSDL/XSD code generation (CXF codegen) for SOAP contracts |
| `skltp-ei-data-model` | JPA entity `Engagement`, `EngagementRepository`, `EngagementSpecifications` |
| `skltp-ei-common` | Shared utilities, `CheckInboundHeadersProcessor`, `SoapActionRemoverInInterceptor`, broker config |
| `skltp-ei-frontend` | Spring Boot app: Update-service and notification-service (input + queuing) |
| `skltp-ei-backend` | Spring Boot app: FindContent, persistence, subscriber routing, cache |
| `skltp-ei-application` | Composite Spring Boot app combining frontend + backend |
| `skltp-ei-teststub` | Mock `GetLogicalAddresses` and `ProcessNotification` endpoints for integration tests |
| `report` | JaCoCo test-coverage aggregation |

---

## Architecture & Message Flow

### Logical Components
- **update-service** (frontend, port 8081): Receives updates from source systems via SOAP/CXF. Validates, then places on JMS queue.
- **notification-service** (frontend, port 8081): Receives `ProcessNotification` from federated EI instances.
- **process-service** (backend): Reads from the `process` queue, persists to database, fans out to per-subscriber notification queues.
- **collect-service** (backend): Aggregates small updates into batches (configurable threshold and timeout) before forwarding to `process` queue.
- **notify-service-N** (backend, dynamic): One Camel route per subscriber; reads from `EI.NOTIFICATION.{logicalAddress}` and calls subscriber's `ProcessNotification` via VP.
- **find-content-service** (backend, port 8082): Synchronous SOAP query endpoint against the database.
- **persistence-service**: Spring Data JPA layer over `engagement_index_table`.
- **init-notifiers**: On startup, queries SKLTP TAK via VP's `GetLogicalAddressesByServiceContract` to build the subscriber list; cached in EhCache.

### Main Data Flow
```
Source system ──SOAP──▶ update-service ──JMS──▶ [collect queue]
                                                       │
                                          (aggregate by size/timeout)
                                                       ▼
Federated EI ──SOAP──▶ notification-service ──JMS──▶ [process queue]
                                                       │
                                          UpdatePersistentStorageProcessor
                                          (upsert DB, generate diff list)
                                                       │
                                          NotificationSplitterBean
                                                 (per subscriber)
                                                       ▼
                                    EI.NOTIFICATION.{logicalAddress} queues
                                                       │
                                         (dynamic Camel route per subscriber)
                                                       ▼
                                      VP ──▶ Subscriber ProcessNotification
```

### FindContent Flow
```
Consumer ──SOAP──▶ find-content-service (port 8082) ──▶ DB query ──▶ SOAP response
```

---

## Domain Model

### Core Entity: `Engagement` (table: `engagement_index_table`)
- **Primary key**: `id` – SHA-256 hex (64 chars) derived from the logical business key
- **Business key fields** (immutable after insert, annotated `@Column(updatable=false)`):
  - `registeredResidentId`, `serviceDomain`, `categorization`, `logicalAddress`
  - `businessObjectInstanceId`, `sourceSystem`, `dataController`, `owner`, `clinicalProcessInterestId`
- **Mutable fields**: `creationTime`, `updateTime`, `mostRecentContent`
- **Search index**: `engagement_search_index` on `(registered_resident_id, service_domain, categorization)`
- Updates are **idempotent** (safe to replay). `update_time` only changes when `most_recent_content` changes.
- Dirty reads allowed on queries (READ_UNCOMMITTED); writes use local DB transactions (not XA).

### Subscriber Model
- Loaded from SKLTP TAK at startup via `GetLogicalAddressesByServiceContract`
- Cached in EhCache with configurable TTL (default 7200s)
- Each subscriber has: logical address, filter rules (service domains), and a JMS queue name `EI.NOTIFICATION.{logicalAddress}`
- A dynamic Camel route is created per subscriber after cache load (event-driven via `SubscriberCacheEventListener`)

---

## SOAP Services & Endpoints

| Service | Port | Path | Direction |
|---|---|---|---|
| Update | 8081 | `/skltp-ei/update-service/v1` | Inbound from source systems |
| ProcessNotification | 8081 | `/skltp-ei/notification-service/v1` | Inbound from federated EI |
| FindContent | 8082 | `/skltp-ei/find-content-service/v1` | Inbound from consumers |
| GetLogicalAddresses | — | Via VP | Outbound to SKLTP TAK |
| ProcessNotification (notify) | — | Via VP | Outbound to subscribers |

### HTTP Management Endpoints
- `GET /skltp-ei/resetcache` – Clears and refreshes subscriber cache
- `GET /skltp-ei/subscriber/status` – Returns subscriber cache as JSON
- `GET /skltp-ei-backend/status` – Backend health
- `GET /skltp-ei-frontend/status` – Frontend health
- `GET /actuator/health` – Spring Boot health (readiness/liveness probes)
- `GET /actuator/hawtio` – Hawtio JMX console
- `GET /actuator/jolokia` – Jolokia REST-to-JMX

---

## Key Camel Routes

| Route Class | Consumer | Purpose |
|---|---|---|
| `EiFrontendRoute` | CXF (Update + ProcessNotification) | Validate → owner-assign → enqueue |
| `EiBackendCollectRoute` | SJMS `collect` queue | Aggregate messages by size (default 1000) or timeout (default 30s) |
| `EiBackendUpdateRoute` | ActiveMQ `process` queue | Persist to DB, split into per-subscriber notifications |
| `EiBackendDynamicNotificationRoute` | `EI.NOTIFICATION.*` (dynamic) | Call VP → subscriber ProcessNotification |
| `EiBackendFindContentRoute` | CXF FindContent | Query DB, return response |
| `EiBackendResetCacheRoute` | Jetty HTTP | Clear EhCache, reload subscribers |
| `EiBackendSubscriberStatusRoute` | Jetty HTTP | Return subscriber status JSON |

---

## Error Handling & Reliability

- **Dead Letter Queues**: `DLQ.{queueName}` for messages that exhaust retries
- **Redelivery policy**: Configurable via `activemq.broker.maximum-redeliveries` and `activemq.broker.redelivery-delay`
- **Circular routing prevention**: Incoming updates whose sender HSA-ID appears in `update-notification.not.allowed.hsaid.list` are dropped to prevent loops between federated EI instances
- **Error codes** (standardised):
  - `EI000` – Technical error
  - `EI002` – Duplicate engagements in request
  - `EI003` – Invalid routing / logical address mismatch
  - `EI004` – Missing mandatory fields or schema validation failure

---

## Configuration & Properties

### Key Properties (backend)
- `server.port` = 8881 (backend), 8882 (frontend)
- `ei.hsa.id` – EI's own HSA identity
- `ei.vp.hsa.id` – VP's HSA identity used as sender when calling VP
- `ei.subscriber.cache.timeToLiveSeconds` – Cache TTL (default 7200)
- `ei.collect.queue.size.threshold` – Threshold for collect vs. process queue routing
- `activemq.broker.url` – ActiveMQ URL (default `vm://localhost` for embedded; failover URL in production)
- `activemq.broker.maximum-redeliveries` – Retry limit (default 5)
- `activemq.broker.redelivery-delay` – Delay in ms (default 1000)

### Helm / Kubernetes
- Values in `helm/values.yaml` – resource limits, replica counts, image tags, probe paths
- ConfigMaps: `ei-backend-configmap-default`, `ei-frontend-configmap-default`, `log4j-configmap`
- Backend ports: 8082 (SOAP), 8083 (mgmt), 8881 (internal)
- Frontend ports: 8081 (SOAP), 8084 (mgmt), 8882 (internal)
- Non-root container user: `ind-app` (UID 1000)
- JVM: `-XX:MaxRAMPercentage=75`

---

## Security Patterns

- **Hawtio** management console requires authentication (`hawtio.authentication.enabled=true`)
- **Header validation**: `CheckInboundHeadersProcessor` enforces required SKLTP/RIVTA headers
- **SOAP Action removal**: `SoapActionRemoverInInterceptor` strips incoming SOAP Action headers
- **Identity headers** set by EI when calling VP:
  - `x-vp-sender-id`, `x-vp-instance-id`
- **Correlation tracking**: `x-skltp-correlation-id` propagated via MDC/ThreadContext through all routes
- **Non-root container**: Dockerfile uses UID 1000 user

---

## Testing

### Running Tests
```bash
mvn clean install                           # Build and unit tests
mvn clean verify -Ptest-coverage            # With JaCoCo coverage (report in /report/target/)
```

### Local Development
1. Start `EiTeststubApplication` (mocks GetLogicalAddresses + ProcessNotification)
2. Start `EiApplication` (composite: frontend + backend)
3. Runs with H2 in-memory DB and embedded ActiveMQ by default
4. Run SoapUI tests from `./test/functional/SoapUI/`

### Test Categories
- **Unit tests**: Standard JUnit in each module
- **Integration tests** (in `skltp-ei-backend/src/test/.../integrationtests/`): Use embedded Camel + Spring Boot + H2 + teststub profile (`@Profile("teststub")`)
- **Functional tests**: SoapUI (`test/functional/SoapUI/`)
- **Non-functional**: Gatling (`test/non-functional/Gatling/`) and load test runner

### Test Profiles
- `teststub` profile activates `EiTeststubRoute` which mocks external SKLTP services

---

## Coding Conventions

- **Lombok** is used throughout: `@Data`, `@Slf4j` / `@Log4j2`, `@Builder`, `@RequiredArgsConstructor`
- **Apache Camel Java DSL** for all route definitions (not XML)
- **Spring Data JPA Specifications** (`EngagementSpecifications`) for dynamic queries – do not write raw JPQL unless necessary
- **JAXB** for SOAP message marshalling/unmarshalling (generated from WSDL via CXF codegen in `skltp-ei-schemas`)
- **Jakarta EE** APIs (not `javax.*`) – Spring Boot 3.x uses Jakarta namespace
- **Log4j2** with ECS layout for structured logging to Elasticsearch
- Correlation IDs propagated via `ThreadContext` (MDC) – always set/clear in Camel processors
- All business key fields on `Engagement` are `@Column(updatable = false)` – never attempt to update them
- The SHA-256 primary key is computed deterministically from the business key – do not assign IDs manually

---

## Domain Terminology (Swedish → English)

| Swedish | English |
|---|---|
| Engagemangsindex | Engagement Index (EI) |
| Källsystem | Source system |
| Prenumerant | Subscriber |
| Tjänstekonsument | Service consumer |
| Tjänsteproducent | Service producer |
| Virtualisgeringsplattform (VP) | Virtualisation Platform (SKLTP service bus) |
| TAK | Service addressing catalogue (routes/permissions registry) |
| Tjänstekontrakt | Service contract (WSDL/RIVTA spec) |
| Registrerad invånare | Registered resident (patient) |
| Logisk adress | Logical address (HSA-ID of a care unit) |
| HSA-id | HSA identity (Swedish healthcare directory ID) |
| Federerat EI | Federated EI instance (another EI that can push/receive notifications) |
| Uppdatering | Update (an indexing event from a source system) |
| Notifiering | Notification (push to subscriber after a processed update) |

---

## Architectural Constraints

1. **EI holds no source data** – it only mirrors index attributes (e.g. "patient X has a booking at unit Y")
2. **Frontend resilience**: The frontend can receive updates even when the backend/DB is down (JMS buffering)
3. **Idempotent upserts**: All DB writes can be safely replayed
4. **No XA transactions**: DB transaction commits first; if queue commit fails, the message is retried and the idempotent upsert ensures correctness
5. **Per-subscriber isolation**: A failing subscriber does not block other subscribers
6. **Scale target**: ~250 million engagement records for the Swedish population
7. **Circular routing**: Prevented by HSA-ID blacklist (`update-notification.not.allowed.hsaid.list`)
8. **Field immutability**: Business key fields cannot change after creation (use delete + re-insert pattern)

---

## RIVTA / Service Contract

- Contract: `itintegration:engagementindex` version 1.0
- WSDL sources: `skltp-ei-schemas/src/main/resources/schemas/TD_ENGAGEMENTINDEX_1_0_R/`
- Infrastructure contract: `INFRASTRUCTURE_ITINTEGRATION_REG_2` (for GetLogicalAddresses)
- All SOAP services follow RIVTA (Swedish interoperability framework) messaging patterns
- All inbound calls must carry RIVTA/SKLTP standard headers (validated by `CheckInboundHeadersProcessor`)
