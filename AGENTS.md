# AGENTS.md – SKLTP Engagement Index (EI)

## Architecture

Multi-module Maven project (Java 17, Spring Boot 3.3.7, Apache Camel 4.8.3 LTS). Two deployable Spring Boot apps:
- **Frontend** (`skltp-ei-frontend`): SOAP endpoints for Update + ProcessNotification → JMS queues
- **Backend** (`skltp-ei-backend`): JMS consumers → DB persistence → per-subscriber notification fan-out + FindContent SOAP query

The composite app (`skltp-ei-application`) runs both in one JVM for local dev. Teststub (`skltp-ei-teststub`) mocks external SKLTP services.

### Data flow
```
Source → Update SOAP (8081) → collect queue → aggregate → process queue → DB upsert → EI.NOTIFICATION.{subscriber} → VP → subscriber
```
FindContent (8082) is a synchronous DB query, independent of the async pipeline.

## Build & Run

```powershell
mvn clean install                        # Build + unit tests
mvn clean verify -Ptest-coverage         # With JaCoCo (report in /report/target/)
```

**Local dev**: Start `EiTeststubApplication` first, then `EiApplication`. Uses H2 in-memory + embedded ActiveMQ by default.

## Key Conventions

- **Camel Java DSL only** – all routes in `*Route.java` classes extending `RouteBuilder`
- **Processors pattern** – business logic in `@Component` classes implementing `Processor`, autowired into routes
- **Entity ID**: SHA-256 hex of business key fields – never assign manually (see `BusinessKey.java` in `skltp-ei-data-model`)
- **Business key fields on `Engagement`** are `@Column(updatable=false)` – use delete + re-insert, never update them
- **Jakarta EE** namespace (`jakarta.*`), not `javax.*`
- **Lombok** everywhere: `@Slf4j`, `@Data`, `@RequiredArgsConstructor`
- **Spring Data JPA Specifications** (`EngagementSpecifications`) for dynamic queries – no raw JPQL
- **Log4j2** with ECS layout; correlation via `x-skltp-correlation-id` in `ThreadContext` (MDC)

## Module Dependency Order

`skltp-ei-schemas` → `skltp-ei-data-model` → `skltp-ei-common` → `skltp-ei-frontend` / `skltp-ei-backend` → `skltp-ei-application`

All dependency versions are declared in the parent `pom.xml` `<properties>`. Child POMs must not declare versions.

## Important Files

| Path                                                                 | Purpose                                                                       |
|----------------------------------------------------------------------|-------------------------------------------------------------------------------|
| `skltp-ei-frontend/.../EiFrontendRoute.java`                         | CXF SOAP consumers, validation, JMS enqueueing                                |
| `skltp-ei-backend/.../route/EiBackendUpdateRoute.java`               | Process queue consumer, DB persist, notification split                        |
| `skltp-ei-backend/.../route/EiBackendCollectRoute.java`              | Aggregation by size/timeout before process queue                              |
| `skltp-ei-backend/.../route/EiBackendDynamicNotificationRoute.java`  | Dynamic per-subscriber Camel routes                                           |
| `skltp-ei-data-model/.../entity/model/Engagement.java`               | JPA entity (table: `engagement_index_table`)                                  |
| `skltp-ei-data-model/.../entity/model/EngagementSpecifications.java` | Spring Data Specification builders for queries                                |
| `skltp-ei-common/.../service/CheckInboundHeadersProcessor.java`      | RIVTA header validation                                                       |
| `skltp-ei-backend/.../subscriber/`                                   | Subscriber cache (EhCache), event listener, dynamic route creation            |
| `helm/`                                                              | Kubernetes deployment (backend ports 8082/8083/8881, frontend 8081/8084/8882) |

## Testing

- Unit tests: standard JUnit in each module
- Integration tests: `skltp-ei-backend/src/test/.../integrationtests/*IT.java` – uses `@Profile("teststub")`, embedded Camel + H2
- Functional tests: SoapUI projects in `test/functional/SoapUI/`

## Pitfalls

- **Circular routing**: Updates whose sender HSA-ID is in `update-notification.not.allowed.hsaid.list` are dropped. Always include `ei.hsa.id` and `vp.hsa.id` in that list.
- **No XA**: DB commits first; JMS failures cause idempotent replay – never add side effects that aren't idempotent.
- **Queue naming**: Notification queues follow pattern `EI.NOTIFICATION.{logicalAddress}` – a dynamic Camel route per subscriber is created at startup via `SubscriberCacheEventListener`.
- **Write lock**: Backend can suspend process/collect routes for zero-downtime DB migration (`/skltp-ei/writelock/*` on mgmt port 8083).
- **Profile split**: `spring.profiles.include=ei-backend` or `ei-frontend` controls which beans load; the composite app includes both.

