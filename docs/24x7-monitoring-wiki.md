## Overview

Team Backbone maintains a [monitoring component](https://dev.azure.com/ndwnu/Backbone/_git/monitoring-backend) that acts as a bridge between our alerting tools (Datadog, Grafana) and TriOpSys for 24/7 incident management. We send webhook alerts to their endpoints, and they process and forward them to TriOpSys.

The monitoring component lives in Backbone's repository. This page documents how team Realtime is integrated.

## 1. Team Registration

A team has been registered for Realtime in the monitoring component using this endpoint:

```
POST https://monitoring.staging.ndw.nu/monitoring-backend/v1/monitoring/teams
```
with the payload:
```
{
    "name": "Realtime"
}
```

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

```
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

**Full (staging) URL for Realtime:**
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
      "environment": "prod",
      "tags": "$TAGS"
    }
  ],
  "commonLabels": {
    "alertname": "$ALERT_TITLE",
    "ruleId": "$ALERT_ID"
  }
}
```

> **Note:** The `environment` is hardcoded because because there are no per-monitor environment variables in Datadog. Maybe a nice-to-have for the future.

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
      "alertTitle": "NCIS-CN: FCD data interrupted",
      "date": 1769433745000,
      "environment": "prod",
      "tags": "fd,monitor,ncis-cn,priority:p1,service:FCD CN receiver,team:dev"
    }
  ],
  "commonLabels": {
    "alertname": "NCIS-CN: FCD data interrupted",
    "ruleId": "d8e9d4934258fbc4"
  }
}
```

### 3.2 Grafana Webhook

```
POST /internal/v1/teams/{teamId}/grafana-receiver
```

**Full (staging) URL for Realtime:**
```
https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/grafana-receiver
```

This endpoint expects a custom payload produced by a Grafana webhook contact point configured with a **Custom Payload template**.

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
- `service_name` — must be added as a custom label on the alert rule, matching the `serviceId` registered in step 2. It cannot contain slashes in the name.

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
      "service": "ignite-tariffs-consumer-deployment",
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

## 4. Setting Up the Webhooks

### 4.1 Configuring the Datadog Webhook

1. In Datadog, go to **Integrations > Webhooks**.
2. Click **New** to create a new webhook.
3. Fill in the following fields:
   - **Name**: Choose a descriptive name, e.g. `monitoring-24x7-prod`
   - **URL**: The full Datadog receiver URL for team Realtime:
     ```
     https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/datadog-receiver
     ```
   - **Payload**: Paste the webhook body template from [section 3.1](#31-datadog-webhook).
   - **Custom Headers**: Add the authorization header:
     ```json
     {
       "Authorization": "Bearer psSf5cQs0hFTIUsTx81uUgyVzyO2OY0rPkZFxTU45Xo="
     }
     ```
4. Click **Save**.

<!-- screenshot: Datadog webhook configuration -->

### 4.2 Using the Datadog Webhook in a Monitor

1. Open the monitor you want to connect to 24/7 monitoring (or create a new one).
2. Scroll to the **Notify your team** section.
3. In the notification message, add the webhook mention:
   ```
   @webhook-monitoring-24x7-prod
   ```
   (The name after `@webhook-` must match the webhook name you created in step 4.1.)
4. Make sure the monitor has a `service` tag that matches the `serviceId` of the registered service (see [section 2](#2-service-registration)). For example: `service:FCD CN receiver`.
5. Save the monitor.

<!-- screenshot: Datadog monitor notification section -->

### 4.3 Configuring the Grafana Webhook Contact Point

1. In Grafana, go to **Alerting > Contact points**.
2. Click **Add contact point**.
3. Fill in the following fields:
   - **Name**: Choose a descriptive name, e.g. `monitoring-24x7-prod`
   - **Integration**: Select **Webhook**.
   - **URL**: The full Grafana receiver URL for team Realtime:
     ```
     https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/grafana-receiver
     ```
   - **Authorization Header** (under Optional Webhook settings): Set the credentials:
     ```
     Bearer psSf5cQs0hFTIUsTx81uUgyVzyO2OY0rPkZFxTU45Xo=
     ```
   - **Custom Payload**: Enable this toggle and paste the Custom Payload template from [section 3.2](#32-grafana-webhook).
   - **Vars**: Add a variable `environment` with value `prod` (or `staging`, depending on the environment).
4. Click **Save contact point**.

<!-- screenshot: Grafana contact point configuration -->

### 4.4 Using the Grafana Webhook in an Alert Rule

1. Go to **Alerting > Alert rules** and open the alert rule you want to connect (or create a new one).
2. In the **Labels** section, add the following custom label:
   - `service_name` = the `serviceId` of the registered service (see [section 2](#2-service-registration)), e.g. `ncis-ci-opc-fcd-194`.
3. Optionally, in the **Annotations** section, add:
   - `summary` = a description for the alert.
4. In **Alerting > Notification policies**, make sure your alert rule is routed to the contact point you created in step 4.3. You can do this either by:
   - Setting the contact point as the **default** contact point, or
   - Creating a **nested policy** that matches on a label (e.g. `team = Realtime`) and routes to the contact point.
5. Save the alert rule / notification policy.

<!-- screenshot: Grafana alert rule labels -->
<!-- screenshot: Grafana notification policy -->
