# Runbook: Suppressing DB Writes During DB Cluster Migration

## Problem

EI backend writes engagement data to a MySQL master database identified by a **Virtual IP (VIP)**. The cluster is master-master replicated; the node that owns the VIP is the authoritative write target.

During migration to a new Kubernetes cluster and new database machine:

1. The new DB machine is pre-replicated from the old one.
2. The VIP is moved from the old machine to the new machine.
3. **During the cutover, the VIP is briefly owned by both machines simultaneously.**

In this window, both the old and new EI backend pods consume from the same ActiveMQ queues and write to what they each believe to be the active master. Because engagement upserts are idempotent, duplicate row writes are safe — but `update_time` / `most_recent_content` timestamps can be set to stale values if the old pod commits a batch after the new pod already committed the same batch. Subscriber notification fan-out can also fire twice per message.

## Solution

Suspend database writes on the **old** backend pod before the VIP moves, using the **Write Lock** management endpoints introduced for this purpose (see [implementation.md](implementation.md)).

Suspending the two Camel queue-consumer routes on the old pod causes:
- The old pod to stop polling ActiveMQ after finishing any in-flight transaction.
- All pending messages to remain durably buffered in ActiveMQ's persistent queues.
- The new pod (which has no write lock) to process all queued messages exclusively and write to the new master.

No messages are lost. The frontend is unaffected and continues accepting updates throughout.

## Write Lock Endpoints (management port 8083)

| Endpoint | Effect |
|---|---|
| `GET /skltp-ei/writelock/enable` | Suspends `backend-collection-route` and `backend-process-route` |
| `GET /skltp-ei/writelock/disable` | Resumes both routes |
| `GET /skltp-ei/writelock/status` | Returns JSON status of the lock and actual Camel route states |

These endpoints are served on the internal management port (8083 in production, 8083 locally) — the same port as `/skltp-ei/resetcache` and `/skltp-ei/subscriber/status`. They are **not** exposed outside the cluster.

### Accessing the endpoints

The management port is a `ClusterIP` service with no ingress. Use one of:

**Option A – `kubectl exec` (recommended)**

The Alpine-based container includes busybox `wget`. Run commands directly inside the pod:
```
kubectl exec -it <pod-name> -- wget -qO- http://localhost:8083/skltp-ei/writelock/status
```

**Option B – `kubectl port-forward` + local browser/curl**

Forward the management port to your workstation:
```
kubectl port-forward <pod-name> 8083:8083
# Then in another terminal or browser:
curl http://localhost:8083/skltp-ei/writelock/status
```

## Migration Procedure

### Prerequisites
- The new DB machine has been fully replicated from the old one and is in sync.
- The new EI backend pod is up and healthy on the new cluster (`GET /actuator/health`).
- Both pods are consuming from the **same** ActiveMQ broker (or the same ActiveMQ cluster).
- You have `kubectl exec` access to both backend pods.

### Step-by-step

```
1.  Confirm old pod is healthy and processing normally.

    kubectl exec -it <old-pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/status
    # Expected: {"writeLockEnabled": false, "routes": {"backend-process-route": "Started", "backend-collection-route": "Started"}}

2.  Enable the write lock on the OLD pod.

    kubectl exec -it <old-pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/enable
    # Expected: "Write lock enabled"

3.  Verify the lock and route states on the old pod.

    kubectl exec -it <old-pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/status
    # Expected: {"writeLockEnabled": true, "routes": {"backend-process-route": "Suspended", "backend-collection-route": "Suspended"}}

4.  Move the VIP to the new database machine.
    The old pod is no longer writing; there is no race condition.

5.  Verify the new pod is processing normally (queue depth should drain).

    kubectl exec -it <new-pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/status
    # Expected: {"writeLockEnabled": false, "routes": {"backend-process-route": "Started", "backend-collection-route": "Started"}}

6.  Terminate (or scale down) the old pod.
    The write lock becomes moot, but /writelock/disable is available if you need
    to temporarily resume the old pod without restarting it (e.g. to rollback).
```

### Rollback

If the migration fails before step 4, disable the write lock and the old pod resumes normally:

```
kubectl exec -it <old-pod> -- wget -qO- http://localhost:8083/skltp-ei/writelock/disable
```

If the migration fails after step 4 (VIP already moved):
- Restore the VIP to the old machine.
- Disable the write lock on the old pod.
- The new pod (if still running) will also write — but both are pointing to the same master again, and idempotent upserts ensure correctness.

## What Is NOT Affected

| Component | Effect |
|---|---|
| Frontend pods | Unaffected; continue accepting updates and placing messages on queues |
| FindContent SOAP service | Unaffected; read-only DB queries continue |
| ActiveMQ message durability | All messages persisted to disk; nothing is lost while routes are suspended |
| Subscriber cache | Unaffected; cache refresh endpoints remain available |
| Dynamic notification routes | No new notifications generated while process route is suspended; they drain via the new pod |
