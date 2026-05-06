# App-of-Apps Helm Configuration Guide

This guide explains how to deploy **SKLTP Engagemangsindex (EI)** using the **app-of-apps** pattern with ArgoCD. It is aimed at operators setting up a new environment from scratch.

For per-key documentation of all EI Helm values, see [Helm Values Reference].

---

## 1. The App-of-Apps Pattern

The SKLTP platform uses the [ArgoCD App-of-Apps](https://argo-cd.readthedocs.io/en/stable/operator-manual/cluster-bootstrapping/#app-of-apps-pattern) approach to manage multiple applications from a single Git repository.

### How It Works

You create a **central repository** (the "app-of-apps" repo) containing a Helm chart. When rendered, this chart produces an ArgoCD **ApplicationSet** resource that drives the deployment of all platform services — including EI.

The **ApplicationSet** uses a **list generator** that iterates over an `applications[]` list in `values.yaml`. For each entry it:

1. Reads `valuefiles/common-values.yaml` (shared settings: image registry, ingress hostnames, SKLTP instance ID).
2. Reads `valuefiles/<name>-values.yaml` (application-specific overrides).
3. Merges both into the `helm.values` field of the generated ArgoCD Application.
4. Points ArgoCD at the application's own Git repository + `helm/` path + pinned tag.

ArgoCD then renders each application's Helm chart (e.g. `ei/helm/`) with the merged values and syncs the resulting Kubernetes resources to the target cluster.

### Value Precedence (highest → lowest)

1. `valuefiles/ei-values.yaml` (environment-specific overrides)
2. `valuefiles/common-values.yaml` (shared across all apps)
3. `ei/helm/values.yaml` (chart defaults in the EI repository)

---

## 2. Setting Up Your App-of-Apps Repository

Create a new Git repository with the following structure:

```
my-platform-apps/
├── Chart.yaml
├── values.yaml
├── valuefiles/
│   ├── common-values.yaml
│   └── ei-values.yaml
└── templates/
    ├── applicationset.yaml
    ├── configmaps/
    │   ├── common-configmap.yaml
    │   ├── ei-backend-configmap.yaml
    │   └── ei-frontend-configmap.yaml
    └── secrets/
        └── (SealedSecrets or references)
```

### 2.1 `Chart.yaml`

```yaml
apiVersion: v2
name: my-platform-applicationset
description: App-of-apps chart for deploying SKLTP services
type: application
version: 0.1.0
appVersion: "0.0.1"
```

### 2.2 `values.yaml` — Cluster & Application List

This is the top-level values file for your app-of-apps chart. It defines the target cluster/namespace and lists which applications to deploy.

```yaml
destination:
  cluster: a                              # CHANGE: cluster identifier
  environment: myenv                      # CHANGE: environment name (dev, qa, prod, etc.)
  project: my-platform-project            # CHANGE: ArgoCD project name
  namespace: my-platform-myenv            # CHANGE: target Kubernetes namespace
  server: https://kubernetes.default.svc

repo:
  path: helm                              # Path within each app repo where Helm chart lives

applications:
- name: ei
  repourl: https://github.com/skltp/ei.git
  targetrevision: v1.0.0                  # CHANGE: pin to desired EI release tag
```

### 2.3 `templates/applicationset.yaml` — The List Generator

This is the core template that generates one ArgoCD Application per entry in `applications[]`. It merges `common-values.yaml` and the per-app values file into the Helm values for each application.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: {{ .Chart.Name }}-{{ .Values.destination.environment }}
  namespace: argocd
spec:
  generators:
  - list:
      elements:
      {{- range .Values.applications }}
      - application: {{ .name }}-{{ $.Values.destination.environment }}
        repourl: {{ .repourl }}
        targetrevision: {{ .targetrevision }}
        app-values: | {{ $.Files.Get "valuefiles/common-values.yaml" | nindent 10 }}
          {{ $.Files.Get (printf "valuefiles/%s-values.yaml" .name) | nindent 10 }}
      {{- end }}
  template:
    metadata:
      name: '{{`{{application}}`}}'
    spec:
      destination:
        namespace: {{ .Values.destination.namespace }}
        server: {{ .Values.destination.server }}
      project: {{ .Values.destination.project }}
      source:
        repoURL: '{{`{{repourl}}`}}'
        path: {{ .Values.repo.path }}
        targetRevision: '{{`{{targetrevision}}`}}'
        helm:
          values: '{{`{{app-values}}`}}'
```

> **Key points about this template:**
> - It uses `$.Files.Get` to read the value files from your app-of-apps repo and inject them as inline Helm values.
> - The double-brace escaping (`{{` `` ` `` `{{...}}` `` ` `` `}}`) is required because the inner `{{application}}`, `{{repourl}}`, etc. are ArgoCD ApplicationSet template parameters — not Go template expressions.
> - Each application gets its own ArgoCD Application resource pointing at the application's own Git repo and Helm chart.

---

## 3. EI Architecture — Two Deployments

Unlike single-deployment applications, EI produces **two** Kubernetes Deployments from a single Helm chart:

| Deployment | Role | SOAP Port | Management Port | Internal Port |
|---|---|---|---|---|
| **ei-backend** | FindContent query endpoint, DB persistence, subscriber notification fan-out | 8082 | 8083 | 8881 |
| **ei-frontend** | Update + ProcessNotification inbound SOAP, JMS enqueueing | 8081 | 8084 | 8882 |

Both deployments share the same Helm release but have **separate** container images, resource limits, ConfigMap references, and probe configurations. This is reflected in the values structure under `container.backend` / `container.frontend`, `deployments.backend` / `deployments.frontend`, and `environment.backend` / `environment.frontend`.

---

## 4. Minimal EI Deployment Configuration

### 4.1 `valuefiles/common-values.yaml` — Shared Values

Settings consumed by EI and potentially other SKLTP services you deploy:

```yaml
repository: registry.example.com/skltp/        # CHANGE: your container registry prefix

skltp:
  instanceId: MY-PLATFORM_ID                   # CHANGE: ID of this SKLTP instance
```

### 4.2 `valuefiles/ei-values.yaml` — EI-Specific Overrides

Minimum overrides for EI:

| Concern               | Keys to set                                                                        |
|-----------------------|------------------------------------------------------------------------------------|
| Scaling               | `deployments.backend.replicaCount`, `deployments.frontend.replicaCount`            |
| Resources             | `deployments.backend.memory`, `deployments.frontend.memory`                        |
| Image tags            | `container.backend.image.tag`, `container.frontend.image.tag`                      |
| VIP service name      | `vip.name`                                                                         |
| Queue prefix          | `queues.prefix`                                                                    |
| Environment variables | `environment.backend.config_maps`, `environment.backend.secrets`                   |
|                       | `environment.frontend.config_maps`, `environment.frontend.secrets`                 |
| Log levels            | `log4j.rootLoggerLevel`, `log4j.loggers[]`                                         |

See section 5 for the full example.

### 4.3 Kubernetes Resources (Created via `templates/`)

These must exist in the target namespace before (or alongside) the EI deployment. Create them as additional templates in your app-of-apps chart:

| Resource                          | Purpose                                                                                       |
|-----------------------------------|-----------------------------------------------------------------------------------------------|
| `ConfigMap/common-configmap`      | Shared settings (e.g. TAK endpoint address) used across services.                             |
| `ConfigMap/ei-backend-configmap`  | Backend-specific overrides: database URL, ActiveMQ URL, redelivery settings.                  |
| `ConfigMap/ei-frontend-configmap` | Frontend-specific overrides: ActiveMQ URL.                                                    |
| `Secret/ei-secrets`               | Database credentials and ActiveMQ credentials.                                                |
| `Secret/common-secrets`           | Shared secrets (if applicable).                                                               |
| `Secret/regcred`                  | Image-pull credentials for the container registry.                                            |

> **Note:** Secrets should be provisioned via SealedSecrets, external-secrets-operator, or your organization's secret management solution. Never commit plaintext secrets to Git.

#### About `regcred` (Image-Pull Secret)

The `regcred` Secret is a Kubernetes `kubernetes.io/dockerconfigjson` secret that stores credentials for authenticating against the container image registry. Without it, the kubelet cannot pull the EI container images and pods will fail with `ErrImagePull` / `ImagePullBackOff`.

The EI Helm chart references this secret via `imagePullSecrets`:

```yaml
imagePullSecrets:
  - name: regcred
```

**Creating `regcred` manually** (for testing/bootstrapping):

```bash
kubectl create secret docker-registry regcred \
  --namespace=<your-namespace> \
  --docker-server=registry.example.com \
  --docker-username=<service-account> \
  --docker-password=<token-or-password>
```

**In production**, use SealedSecrets or an external-secrets-operator to manage this secret declaratively. The secret must exist in the same namespace as the EI Deployments.

---

## 5. Complete Minimal App-of-Apps Example

Below is a self-contained set of all files needed in your app-of-apps repository to deploy EI. Each file is separated by `---` with a header comment.

> Replace placeholder values (marked with `# CHANGE`) with your environment-specific settings.

```yaml
##############################################################################
# FILE: Chart.yaml
##############################################################################
apiVersion: v2
name: my-platform-applicationset
description: App-of-apps chart for deploying SKLTP services
type: application
version: 0.1.0
appVersion: "0.0.1"
---
##############################################################################
# FILE: values.yaml — Cluster & application list
##############################################################################
destination:
  cluster: a                                    # CHANGE: cluster identifier
  environment: myenv                            # CHANGE: environment name
  project: my-platform-project                  # CHANGE: ArgoCD project
  namespace: my-platform-myenv                  # CHANGE: target namespace
  server: https://kubernetes.default.svc

repo:
  path: helm

applications:
- name: ei
  repourl: https://github.com/skltp/ei.git
  targetrevision: v1.0.0                       # CHANGE: desired EI version
---
##############################################################################
# FILE: templates/applicationset.yaml — The list generator
##############################################################################
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: {{ .Chart.Name }}-{{ .Values.destination.environment }}
  namespace: argocd
spec:
  generators:
  - list:
      elements:
      {{- range .Values.applications }}
      - application: {{ .name }}-{{ $.Values.destination.environment }}
        repourl: {{ .repourl }}
        targetrevision: {{ .targetrevision }}
        app-values: | {{ $.Files.Get "valuefiles/common-values.yaml" | nindent 10 }}
          {{ $.Files.Get (printf "valuefiles/%s-values.yaml" .name) | nindent 10 }}
      {{- end }}
  template:
    metadata:
      name: '{{`{{application}}`}}'
    spec:
      destination:
        namespace: {{ .Values.destination.namespace }}
        server: {{ .Values.destination.server }}
      project: {{ .Values.destination.project }}
      source:
        repoURL: '{{`{{repourl}}`}}'
        path: {{ .Values.repo.path }}
        targetRevision: '{{`{{targetrevision}}`}}'
        helm:
          values: '{{`{{app-values}}`}}'
---
##############################################################################
# FILE: valuefiles/common-values.yaml — Shared values for all applications
##############################################################################
repository: registry.example.com/skltp/         # CHANGE: your registry prefix

skltp:
  instanceId: MY-PLATFORM_ID                    # CHANGE: SKLTP instance identity
---
##############################################################################
# FILE: valuefiles/ei-values.yaml — EI-specific overrides
##############################################################################
container:
  backend:
    image:
      tag:                                      # CHANGE: backend image tag
  frontend:
    image:
      tag:                                      # CHANGE: frontend image tag

# Environment-specific backwards-compatible service name
vip:
  name: ind-myenv-ei-vip                        # CHANGE: legacy service name

queues:
  prefix: skltp.ei.myenv                        # CHANGE: unique queue prefix per environment

deployments:
  backend:
    replicaCount: 1
    elasticGrokFilter: camel
    memory: 1Gi
  frontend:
    replicaCount: 1
    elasticGrokFilter: camel
    memory: 512Mi

environment:
  backend:
    config_maps:
      - common-configmap
      - ei-backend-configmap
    secrets:
      - common-secrets
      - ei-secrets
  frontend:
    config_maps:
      - common-configmap
      - ei-frontend-configmap
    secrets:
      - common-secrets
      - ei-secrets

log4j:
  rootLoggerLevel: WARN
  loggers:
    - name: se.skltp.ei
      level: INFO
    - name: se.skltp.ei.subscriber
      level: INFO
    - name: se.skltp.ei.service.GetLogicalAddresseesServiceClient
      level: WARN
    - name: se.skltp.ei.EiApplication
      level: INFO
    - name: eiBackendLog
      level: DEBUG
    - name: eiFrontendLog
      level: DEBUG
    - name: org.apache.camel
      level: INFO
---
##############################################################################
# FILE: templates/configmaps/common-configmap.yaml
##############################################################################
apiVersion: v1
kind: ConfigMap
metadata:
  name: common-configmap
  namespace: {{ .Values.destination.namespace }}
data:
  TAKCACHE_ENDPOINT_ADDRESS: "http://tak-services-svc:8080/tak-services/SokVagvalsInfo/v2"
---
##############################################################################
# FILE: templates/configmaps/ei-backend-configmap.yaml
##############################################################################
apiVersion: v1
kind: ConfigMap
metadata:
  name: ei-backend-configmap
  namespace: {{ .Values.destination.namespace }}
data:
  # MySQL database connection
  SPRING_DATASOURCE_URL: "jdbc:mysql://my-mysql-host:3306/ei?connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=false"  # CHANGE

  # ActiveMQ broker URL
  ACTIVEMQ_BROKER_URL: "failover:(tcp://my-activemq-host:61616)?randomize=false&timeout=10000&jms.prefetchPolicy.queuePrefetch=5"  # CHANGE

  # Process/collect redelivery policy
  ACTIVEMQ_BROKER_MAXIMUM-REDELIVERIES: "2"
  ACTIVEMQ_BROKER_REDELIVERY_DELAY: "10000"
  ACTIVEMQ_BROKER_USE_EXPONENTIAL_BACKOFF: "false"

  # Notification redelivery policy (exponential back-off)
  ACTIVEMQ_BROKER_NOTIFICATION_MAXIMUM-REDELIVERIES: "8"
  ACTIVEMQ_BROKER_NOTIFICATION_REDELIVERY_DELAY: "60000"
  ACTIVEMQ_BROKER_NOTIFICATION_USE_EXPONENTIAL_BACKOFF: "true"
  ACTIVEMQ_BROKER_NOTIFICATION_BACKOFF_MULTIPLIER: "2"
  ACTIVEMQ_BROKER_NOTIFICATION_MAXIMUM_REDELIVERY_DELAY: "900000"
---
##############################################################################
# FILE: templates/configmaps/ei-frontend-configmap.yaml
##############################################################################
apiVersion: v1
kind: ConfigMap
metadata:
  name: ei-frontend-configmap
  namespace: {{ .Values.destination.namespace }}
data:
  # ActiveMQ broker URL (frontend only needs to enqueue)
  ACTIVEMQ_BROKER_URL: "failover:(tcp://my-activemq-host:61616)?randomize=false&timeout=10000&jms.prefetchPolicy.queuePrefetch=5"  # CHANGE
---
##############################################################################
# FILE: templates/secrets/ei-secrets.yaml (placeholder — use SealedSecret)
##############################################################################
# apiVersion: bitnami.com/v1alpha1
# kind: SealedSecret
# metadata:
#   name: ei-secrets
#   namespace: {{ .Values.destination.namespace }}
# spec:
#   encryptedData:
#     activemq.broker.user: <sealed-value>
#     activemq.broker.password: <sealed-value>
#     spring.datasource.username: <sealed-value>
#     spring.datasource.password: <sealed-value>
---
##############################################################################
# FILE: templates/secrets/regcred.yaml (placeholder — use SealedSecret)
##############################################################################
# apiVersion: bitnami.com/v1alpha1
# kind: SealedSecret
# metadata:
#   name: regcred
#   namespace: {{ .Values.destination.namespace }}
# spec:
#   encryptedData:
#     .dockerconfigjson: <sealed-value>
#   template:
#     type: kubernetes.io/dockerconfigjson
```

---

## 6. Deployment Workflow

1. **Create your app-of-apps repository** — Use the structure and files from section 5.
2. **Provision secrets** — Create SealedSecrets (or use your secrets operator) for database credentials, ActiveMQ credentials, and image-pull credentials.
3. **Register in ArgoCD** — Create an ArgoCD Application that points at your app-of-apps repository (the "root" application). ArgoCD will render the chart, producing the ApplicationSet.
4. **Sync** — ArgoCD detects the ApplicationSet, generates one Application per entry in `applications[]`, renders each app's Helm chart with the merged values, and applies the resources to the cluster.
5. **Verify** — Check pod status for both `ei-backend` and `ei-frontend` deployments, Actuator health endpoints, and subscriber cache initialisation logs.

### Registering the Root Application in ArgoCD

The "root application" is the single ArgoCD Application that bootstraps everything else. It tells ArgoCD where your app-of-apps repository lives and how to render it. Without this, ArgoCD has no knowledge of your chart.

You can create the root application declaratively or via the ArgoCD UI/CLI:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: my-platform-apps
  namespace: argocd
spec:
  project: my-platform-project                                       # CHANGE: must exist in ArgoCD
  source:
    repoURL: https://git.example.com/my-org/my-platform-apps.git     # CHANGE: your app-of-apps repo
    path: .                                                          # Chart.yaml is at the repo root
    targetRevision: main                                             # CHANGE: branch or tag to track
  destination:
    server: https://kubernetes.default.svc                           # The cluster ArgoCD runs on
    namespace: argocd                                                # ApplicationSet is created here
  syncPolicy:
    automated:
      prune: true       # Remove resources ArgoCD no longer manages
      selfHeal: true    # Revert manual drift automatically
```

Once this root Application is synced, ArgoCD renders your `Chart.yaml` + `values.yaml` + templates, producing the ApplicationSet which in turn creates the EI Application (and any other applications you list).

---

## 7. Additional Override Examples

### 7.1 Resource Limits

```yaml
deployments:
  backend:
    replicaCount: 2
    memory: 2Gi
  frontend:
    replicaCount: 2
    memory: 1Gi
```

### 7.2 Log Levels

```yaml
log4j:
  rootLoggerLevel: WARN
  loggers:
    - name: se.skltp.ei
      level: INFO
    - name: se.skltp.ei.subscriber
      level: DEBUG
    - name: eiBackendLog
      level: DEBUG
    - name: eiFrontendLog
      level: DEBUG
    - name: org.apache.camel
      level: WARN
```

### 7.3 Queue Prefix

Use a unique queue prefix per environment to avoid cross-environment message leakage when sharing an ActiveMQ broker:

```yaml
queues:
  prefix: skltp.ei.prod
```

This produces queue names like `skltp.ei.prod.collect`, `skltp.ei.prod.process`, and `skltp.ei.prod.notification.{logicalAddress}`.

### 7.4 ActiveMQ Redelivery Tuning

Override in `ei-backend-configmap` to adjust retry behaviour:

```yaml
# In templates/configmaps/ei-backend-configmap.yaml:
data:
  # Process/collect: retry 3 times with 5-second delay
  ACTIVEMQ_BROKER_MAXIMUM-REDELIVERIES: "3"
  ACTIVEMQ_BROKER_REDELIVERY_DELAY: "5000"
  ACTIVEMQ_BROKER_USE_EXPONENTIAL_BACKOFF: "false"

  # Notifications: exponential back-off up to 15 minutes
  ACTIVEMQ_BROKER_NOTIFICATION_MAXIMUM-REDELIVERIES: "8"
  ACTIVEMQ_BROKER_NOTIFICATION_REDELIVERY_DELAY: "60000"
  ACTIVEMQ_BROKER_NOTIFICATION_USE_EXPONENTIAL_BACKOFF: "true"
  ACTIVEMQ_BROKER_NOTIFICATION_BACKOFF_MULTIPLIER: "2"
  ACTIVEMQ_BROKER_NOTIFICATION_MAXIMUM_REDELIVERY_DELAY: "900000"
```

### 7.5 CXF Tuning via JAVA_OPTS

If large payloads are expected, increase the CXF child-element limit by adding to a ConfigMap:

```yaml
# In templates/configmaps/ei-backend-configmap.yaml (or separate configmap):
data:
  JAVA_OPTS: '-Dorg.apache.cxf.stax.maxChildElements=150000'
```

---

## 8. EI-Specific Operational Notes

### 8.1 Write Lock (Zero-Downtime DB Migration)

The backend supports a write-lock mechanism to pause queue consumption during database migrations:

- `GET /skltp-ei/writelock/enable` — Suspend process/collect routes
- `GET /skltp-ei/writelock/disable` — Resume routes
- `GET /skltp-ei/writelock/status` — Check current lock state

These endpoints are exposed on the management port (8083) and are not accessible externally.

### 8.2 Secrets Required

The `ei-secrets` secret must provide these keys:

| Key                          | Purpose                 |
|------------------------------|-------------------------|
| `activemq.broker.user`       | ActiveMQ username       |
| `activemq.broker.password`   | ActiveMQ password       |
| `spring.datasource.username` | MySQL database username |
| `spring.datasource.password` | MySQL database password |

---

## See Also

- [Helm Values Reference] — complete per-key documentation of `helm/values.yaml`.
- [Configuration Reference](config.md) — application-level properties documentation.
- [DB Migration Runbook](../db-disable-doc/db-migration-writelock-runbook.md) — write-lock procedure for zero-downtime migrations.

[//]: # (Reference links)

[Helm Values Reference]: <helm_values.md>

