# notifications-api

A multi-tenant notifications service. Customers (tenants) send transactional
notifications — order confirmations, password resets, alerts — through a
single API; we handle templating, channel selection (email / SMS / webhook),
delivery, retries, and audit.

## Architecture

```
                  ┌──────────────────┐
  POST /v1/send   │  NotificationsApi│
  ───────────────▶│   (controller)   │
                  └────────┬─────────┘
                           ▼
                  ┌──────────────────┐
                  │ NotificationCore │  validate tenant, render template,
                  │   (dispatcher)   │  pick channel, hand to delivery
                  └────────┬─────────┘
                           ▼
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
   EmailChannel        SmsChannel       WebhookChannel
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
                  ┌──────────────────┐
                  │  DeliveryTracker │  status, retries (RetryPolicy),
                  │                  │  audit log
                  └──────────────────┘
```

## Modules

| Package | Responsibility |
| --- | --- |
| `api` | REST endpoints, request/response DTOs, validation |
| `core` | `NotificationService` — orchestrates a send end-to-end |
| `channels` | `EmailChannel`, `SmsChannel`, `WebhookChannel` adapters |
| `templates` | `TemplateEngine` — variable substitution, escaping |
| `tenants` | `TenantService`, per-tenant `RateLimiter` |
| `delivery` | `DeliveryTracker`, `RetryPolicy`, exponential backoff |
| `audit` | `AuditLog` — write-only event trail |

## Running locally

```
./mvnw spring-boot:run
```

Then:

```
curl -X POST http://localhost:8080/v1/send \
  -H "X-Tenant-Id: acme" \
  -H "Content-Type: application/json" \
  -d '{
        "channel": "email",
        "to": "alex@example.com",
        "template": "order-confirmation",
        "variables": { "name": "Alex", "order_id": "1234" }
      }'
```

## Tests

Full suite is in the 10–15 minute range. Most are fast unit tests; the
integration tests under `src/test/java/com/cloudbees/demo/notifications/integration`
exercise async delivery, retries, and rate limiting end-to-end and dominate
the wall clock.

```
./mvnw test                  # full suite
./mvnw test -Dtest=*UnitTest # unit tests only
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Code owners are defined in
[CODEOWNERS](.github/CODEOWNERS).

## License

Internal demo asset. Not licensed for redistribution.
