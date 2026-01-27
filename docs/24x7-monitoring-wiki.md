## Overzicht

Team Backbone beheert een [monitoring-component](https://dev.azure.com/ndwnu/Backbone/_git/monitoring-backend) dat als brug fungeert tussen onze alertingtools (Datadog, Grafana) en TriOpSys voor 24/7 incidentbeheer. Wij sturen webhook-alerts naar hun endpoints, en zij verwerken en sturen deze door naar TriOpSys.

Het monitoring-component staat in de repository van Backbone. Deze pagina beschrijft hoe team Realtime is geïntegreerd.

## 1. Teamregistratie

Er is een team geregistreerd voor Realtime in het monitoring-component via dit endpoint:

```
POST https://monitoring.staging.ndw.nu/monitoring-backend/v1/monitoring/teams
```
met de payload:
```
{
    "name": "Realtime"
}
```

| Veld | Waarde |
|------|--------|
| **Team ID** | `f7d5b810-24df-479f-92f6-48dead2428de` |
| **Teamnaam** | Realtime |
| **Token** | `psSf5cQs0hFTIUsTx81uUgyVzyO2OY0rPkZFxTU45Xo=` |

Het **Team ID** wordt gebruikt als path-parameter in alle webhook-URL's.
Het **Token** wordt gebruikt als Bearer-token voor authenticatie.

## 2. Serviceregistratie

Elke service die 24/7-monitoring nodig heeft, moet geregistreerd worden in het monitoring-component. Dit gaat via het serviceregistratie-endpoint:

```
POST https://monitoring.staging.ndw.nu/monitoring-backend/v1/monitoring/teams/f7d5b810-24df-479f-92f6-48dead2428de/services
```

Voorbeeld-payload:

```
{
    "name": "FCD CN receiver",
    "monitoringServiceLevel": "24x7",
    "serviceId": "ncis-ci-opc-fcd-194",
    "serviceType": "supplier",
    "metadata": {}
}
```

| Veld | Beschrijving |
|------|--------------|
| `name` | Weergavenaam van de service |
| `monitoringServiceLevel` | Serviceniveau, bijv. `24x7` |
| `serviceId` | Unieke identifier voor de service (moet overeenkomen met de service-tag/label in Datadog/Grafana) |
| `serviceType` | Type service, bijv. `supplier` |
| `metadata` | Optioneel metadata-object |

## 3. Webhook-endpoints

Er zijn twee webhook-receiver-endpoints beschikbaar, één voor elke monitoringtool. Beide vereisen Bearer-token-authenticatie met het teamtoken.

**Base URL (staging):** `https://monitoring.staging.ndw.nu/monitoring-backend`

Voor de volledige API-specificatie, zie de [OpenAPI-spec](../src/main/resources/openapi.yaml) in de repository.

### 3.1 Datadog Webhook

```
POST /internal/v1/teams/{teamId}/datadog-receiver
```

**Volledige (staging) URL voor Realtime:**
```
https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/datadog-receiver
```

**Webhook body-template** (configureer dit in de Datadog webhook-integratie):

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

> **Let op:** De `environment` is hardcoded omdat er geen per-monitor omgevingsvariabelen beschikbaar zijn in Datadog. Dit is een mogelijke verbetering voor de toekomst.

**Hoe de service wordt geïdentificeerd:** De servicenaam wordt server-side geëxtraheerd uit de `service:`-tag in het `$TAGS`-veld (bijv. `service:FCD CN receiver`). Zorg ervoor dat de Datadog-monitor een `service`-tag bevat die overeenkomt met de `serviceId` die in stap 2 is geregistreerd.

**Mogelijke `alertTransition`-waarden:**

| Waarde | Betekenis |
|--------|-----------|
| `Triggered` | Alert is getriggerd |
| `Warn` | Alert is in waarschuwingsstatus |
| `Recovered` | Alert is hersteld |
| `No Data` | Geen data ontvangen |
| `Re-Triggered` | Alert is opnieuw getriggerd |

**Voorbeeld recovered-payload:**

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

**Volledige (staging) URL voor Realtime:**
```
https://monitoring.staging.ndw.nu/monitoring-backend/internal/v1/teams/f7d5b810-24df-479f-92f6-48dead2428de/grafana-receiver
```

Dit endpoint verwacht een custom payload die wordt geproduceerd door een Grafana webhook-contactpunt dat is geconfigureerd met een **Custom Payload-template**.

**Custom Payload-template** (configureer dit in het Grafana webhook-contactpunt):

```
{{ $alerts := coll.Slice }}
{{ range .Alerts }}
{{ $alerts = coll.Append (coll.Dict "status" .Status "fingerprint" .Fingerprint "alertname" (index .Labels "alertname") "service" (index .Labels "service_name") "environment" $.Vars.environment "description" (index .Annotations "summary") "startsAt" .StartsAt "endsAt" .EndsAt) $alerts }}
{{ end }}
{{ coll.Dict "alerts" $alerts "commonLabels" (coll.Dict "alertname" (index .CommonLabels "alertname") "ruleId" (index .CommonLabels "alertname")) | data.ToJSON }}
```

**Vars** (geconfigureerd per Grafana webhook-contactpunt):
- `environment` — stel in op `prod` voor productie, `staging` voor staging, etc.

**Vereiste Grafana alert rule-labels:**
- `alertname` — standaard Grafana-label (altijd aanwezig), wordt gebruikt als alert-ID
- `service_name` — moet als custom label worden toegevoegd aan de alert rule, overeenkomend met de `serviceId` die in stap 2 is geregistreerd. De naam mag geen slashes bevatten.

**Optionele annotaties:**
- `summary` — wordt gebruikt als alertbeschrijving

**Mogelijke `status`-waarden:**

| Waarde | Betekenis |
|--------|-----------|
| `firing` | Alert is actief |
| `resolved` | Alert is opgelost |

**Voorbeeld firing-payload:**

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
