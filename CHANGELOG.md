# Changelog

## [1.4.2-SNAPSHOT] — unreleased
### Changed
- Retry policy now reads max-attempts from per-tenant config (rolling out)
- Template engine: tightened variable escaping for HTML contexts

## [1.4.1] — 2026-04-22
### Fixed
- Webhook channel HMAC header missed on retries
- Rate limiter window reset off-by-one for daily-cap tenants

## [1.4.0] — 2026-04-08
### Added
- Per-tenant rate limits on the send endpoint
- Audit log entries for delivery state transitions

## [1.3.0] — 2026-03-19
### Added
- SMS channel (Twilio adapter)
- Idempotency key support on POST /v1/send

## [1.2.0] — 2026-02-26
### Added
- Webhook channel with HMAC signing
- Template variable interpolation
