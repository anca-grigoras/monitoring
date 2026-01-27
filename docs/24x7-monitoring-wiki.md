# 24/7 Monitoring - Realtime Team Setup

## Overview

Team Backbone maintains a monitoring component that acts as a bridge between our alerting tools (Datadog, Grafana) and TriOpSys for 24/7 incident management. We send webhook alerts to their endpoints, and they forward them to TriOpSys.

The monitoring component lives in Backbone's repository. This page documents how team Realtime is integrated.

## 1. Team Registration

A team has been registered for Realtime in the monitoring component:

| Field | Value |
|-------|-------|
| **Team ID** | `f7d5b810-24df-479f-92f6-48dead2428de` |
| **Team name** | Realtime |
| **Token** | `psSf5cQs0hFTIUsTx81uUgyVzyO2OY0rPkZFxTU45Xo=` |

The **Team ID** is used as a path parameter in all webhook URLs.
The **Token** is used as a Bearer token for authentication.

## 2. Service Registration

Each service that needs 24/7 monitoring must be registered in the monitoring component. This is done via the service registration endpoint:

```
POST https://monitoring.staging.ndw.nu/monitoring-backend/v1/monitoring/teams/f7d5b810-24df-479f-92f6-48dead2428de/services
```

Example payload:

```json
{
    "name": "FCD CN receiver",
    "monitoringServiceLevel": "24x7",
    "serviceId": "ncis-ci-opc-fcd-194",
    "serviceType": "supplier",
    "metadata": {}
}
```

| Field | Description |
|-------|-------------|
| `name` | Display name of the service |
| `monitoringServiceLevel` | Service level, e.g. `24x7` |
| `serviceId` | Unique identifier for the service (must match the service tag/label in Datadog/Grafana) |
| `serviceType` | Type of service, e.g. `supplier` |
| `metadata` | Optional metadata object |

## 3. Webhook Endpoints

Two webhook receiver endpoints are available, one for each monitoring tool. Both require Bearer token authentication using the team token.

**Base URL (staging):** `https://monitoring.staging.ndw.nu/monitoring-backend`

For the full API specification, see the [OpenAPI spec](../src/main/resources/openapi.yaml) in the repository.

### 3.1 Datadog Webhook

```
POST /internal/v1/teams/{teamId}/datadog-receiver
```

**Full URL for Realtime:**
```
https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/datadog-receiver
```

**Webhook body template** (configure this in the Datadog webhook integration):

```json
{
  "alerts": [
    {
      "alertTransition": "$ALERT_TRANSITION",
      "alertId": "$ALERT_ID",
      "alertScope": "$ALERT_SCOPE",
      "alertTitle": "$ALERT_TITLE",
      "date": "$DATE",
      "environment": "$ENV",
      "tags": "$TAGS"
    }
  ],
  "commonLabels": {
    "alertname": "$ALERT_TITLE",
    "ruleId": "$ALERT_ID"
  }
}
```

> **Note:** The `environment` field uses the `$ENV` variable, but Datadog does not support per-monitor environment variables. This means the environment value is determined by how the webhook is configured, effectively making it hardcoded per webhook instance.

**How the service is identified:** The service name is extracted server-side from the `service:` tag in the `$TAGS` field (e.g., `service:FCD CN receiver`). Make sure the Datadog monitor includes a `service` tag that matches the `serviceId` registered in step 2.

**Possible `alertTransition` values:**

| Value | Meaning |
|-------|---------|
| `Triggered` | Alert has been triggered |
| `Warn` | Alert is in warning state |
| `Recovered` | Alert has recovered |
| `No Data` | No data received |
| `Re-Triggered` | Alert has been re-triggered |

**Example resolved payload:**

```json
{
  "alerts": [
    {
      "alertTransition": "Recovered",
      "alertId": "d8e9d4934258fbc4",
      "alertScope": "",
      "alertTitle": "[TEST] NCIS-CN: FCD data interrupted",
      "date": 1769433745000,
      "environment": "prod",
      "tags": "fd,monitor,ncis-cn,priority:p1,service:FCD CN receiver,team:dev"
    }
  ],
  "commonLabels": {
    "alertname": "[TEST] NCIS-CN: FCD data interrupted",
    "ruleId": "d8e9d4934258fbc4"
  }
}
```

### 3.2 Grafana Webhook

```
POST /internal/v1/teams/{teamId}/grafana-receiver
```

**Full URL for Realtime:**
```
https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/grafana-receiver
```

This endpoint expects a custom payload produced by a Grafana webhook contact point configured with a **Custom Payload template** (requires Grafana 12+).

**Custom Payload template** (configure this in the Grafana webhook contact point):

```
{{ $alerts := coll.Slice }}
{{ range .Alerts }}
{{ $alerts = coll.Append (coll.Dict "status" .Status "fingerprint" .Fingerprint "alertname" (index .Labels "alertname") "service" (index .Labels "service_name") "environment" $.Vars.environment "description" (index .Annotations "summary") "startsAt" .StartsAt "endsAt" .EndsAt) $alerts }}
{{ end }}
{{ coll.Dict "alerts" $alerts "commonLabels" (coll.Dict "alertname" (index .CommonLabels "alertname") "ruleId" (index .CommonLabels "alertname")) | data.ToJSON }}
```

**Vars** (configured per Grafana webhook contact point):
- `environment` — set to `prod` for production, `staging` for staging, etc.

**Required Grafana alert rule labels:**
- `alertname` — standard Grafana label (always present), used as alert ID
- `service_name` — must be added as a custom label on the alert rule, matching the `serviceId` registered in step 2

**Optional annotations:**
- `summary` — used as the alert description

**Possible `status` values:**

| Value | Meaning |
|-------|---------|
| `firing` | Alert is currently firing |
| `resolved` | Alert has been resolved |

**Example firing payload:**

```json
{
  "alerts": [
    {
      "status": "firing",
      "fingerprint": "326ea703b01f6100",
      "alertname": "WARN|ERROR logging",
      "service": "ncis/ignite-tariffs-consumer-deployment",
      "environment": "prod",
      "description": "Notification test",
      "startsAt": "2026-01-26T19:29:52.490787568Z",
      "endsAt": "0001-01-01T00:00:00Z"
    }
  ],
  "commonLabels": {
    "alertname": "WARN|ERROR logging",
    "ruleId": "WARN|ERROR logging"
  }
}
```

## 4. Authentication

All requests require a `Authorization: Bearer <token>` header. For team Realtime, use:

```
Authorization: Bearer psSf5cQs0hFTIUsTx81uUgyVzyO2OY0rPkZFxTU45Xo=
```

## 5. Response Codes

| Code | Meaning |
|------|---------|
| `204` | Alert received and processed successfully |
| `400` | Invalid request (validation error) |
| `401` | Unauthorized (invalid or missing token) |
| `404` | Service not found (service not registered for this team) |
