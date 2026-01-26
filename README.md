# Monitoring

NDW Realtime Monitoring Application - A Spring Boot service for handling monitoring alerts.

## Overview

This application provides webhook endpoints to receive and process alerts from monitoring systems like Datadog.

## Requirements

- Java 21
- Maven 3.x

## Building

```bash
mvn clean install
```

## Code Formatting

This project uses [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) with Google Java Format (AOSP style) to enforce consistent code formatting.

**Check formatting** (fails if code is not formatted):

```bash
mvn spotless:check
```

**Auto-fix formatting**:

```bash
mvn spotless:apply
```

## Running

```bash
mvn spring-boot:run
```

The application starts on port `8080` by default.

## API Endpoints

### Datadog Alert Webhook

```
POST /webhooks/alerts/datadog
```

Receives alert notifications from Datadog.

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| alertId | string | Yes | Unique identifier for the alert |
| alertTitle | string | Yes | Title of the alert |
| alertTransition | string | Yes | Alert state transition (e.g., Triggered, Recovered) |
| alertQuery | string | No | The query that triggered the alert |
| alertMetric | string | No | The metric being monitored |
| alertPriority | string | No | Priority level of the alert |
| alertScope | string | No | Scope of the alert |
| hostname | string | No | Host that triggered the alert |
| tags | string | No | Comma-separated tags (format: key:value) |
| link | string | No | Link to the alert in Datadog |
| date | string | Yes | Timestamp of the alert |
| orgId | string | No | Datadog organization ID |
| orgName | string | No | Datadog organization name |
| message | string | No | Alert message |
| team | string | Yes | Team responsible for handling the alert |

**Response:** `204 No Content`

## Configuration

Configuration is managed via `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| server.port | 8080 | HTTP server port |