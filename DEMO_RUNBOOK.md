# Story-led Smart Tests demo — runbook

This repo is the working asset for the story-led Smart Tests demo. Use it
together with a pre-seeded Smart Tests workspace and the
[project page](https://cloudbees.atlassian.net/wiki/spaces/PRODUCT/pages/6397689958).

## What's in here

| Path | What it is |
| --- | --- |
| `src/main/...` | Notifications API source — a multi-tenant B2B SaaS notifications service (email/SMS/webhook) |
| `src/test/...` | ~100 tests across unit and integration; 10 designed-to-fail on `main` |
| `.github/workflows/nightly-full.yml` | "Overnight" full-suite run (the morning failures in the story) |
| `.github/workflows/pr-pts.yml` | PR pipeline with Smart Tests PTS at 50% target |
| `.demo/setup-demo.sh` | One-shot script to bootstrap `main` history and the `fix/...` branch |
| `.demo/fix-branch.patch` | The patch applied to create the fix branch |

## Fork before every demo

This repo is the **source of truth**. Each demo lead should fork it (or
re-fork it) before running the demo so the base repo is never modified by
demo activity. Work in your fork, push to your fork, run CI in your fork.

## One-time setup per demo

> **Note on the delivered folder:** if you got this repo as a folder
> (rather than cloning from GitHub), the folder may contain a stray
> partial `.git/` directory from how it was packaged. Delete it first:
> `rm -rf .git`. Then proceed from step 1.

1. Push this repo (without `.git`) to a fresh GitHub repo under your own
   account / org. This is your **base repo** — never run demos directly
   against it.
2. Fork that base repo before each demo, and clone the fork locally.
3. Run the bootstrap script:

   ```
   ./.demo/setup-demo.sh
   ```

   This creates a believable commit history on `main`, then creates the
   `fix/retry-and-template-bugs` branch with the fixes pre-applied.

4. Push both branches to your fork:

   ```
   git push origin main
   git push origin fix/retry-and-template-bugs
   ```

5. In your fork's repo settings → Secrets → Actions, add:
   - `LAUNCHABLE_TOKEN` — Smart Tests API token for the seeded workspace
6. In repo settings → Variables → Actions, add:
   - `LAUNCHABLE_ORGANIZATION` — Smart Tests organization name
   - `LAUNCHABLE_WORKSPACE` — Smart Tests workspace name (the seeded one)

7. Manually dispatch `nightly-full` once. This populates your Smart Tests
   workspace with the "last night's CI" session that the demo opens with.
   Confirm in the workspace that you see 10 failures clustered into 2
   issues under the Issues tab.

## The two bugs

Designed so log-based analysis groups the 10 failures into 2 clean root
causes:

| Cluster | Root cause | Failing tests |
| --- | --- | --- |
| Template render | `TemplateEngine.VAR_PATTERN` dropped underscore in v1.4.0; templates using `{{order_id}}`, `{{reset_link}}`, `{{invoice_id}}` throw `TemplateRenderException` | 3 in `TemplateEngineUnitTest`, 1 in `TemplateRepositoryUnitTest`, 1 in `TemplateRenderingIntegrationTest` |
| Retry policy | `RetryPolicy` NPEs when a tenant has no per-tenant `DeliveryConfig`; the `initech` tenant trips it on every send | 3 in `RetryPolicyUnitTest`, 2 in `RetryBehaviorIntegrationTest` |

The fix branch addresses both — `RetryPolicy` falls back to service
defaults, and `TemplateEngine`'s regex restores underscore support.

## Demo flow ↔ repo activity

| Story beat | What the demo lead does in this repo |
| --- | --- |
| Morning: 10 failures from overnight | Open the Smart Tests workspace; the most recent session is yesterday's `nightly-full` run with 10 reds |
| Pivot to Issues tab → 2 root causes | Click into the Issues tab; AI clustering shows the two groups above |
| Developer fixes the issues | `git checkout fix/retry-and-template-bugs` locally, or just narrate it; the fix is already on the branch |
| Run only the relevant subset | Push `fix/retry-and-template-bugs` to your fork (`git push origin fix/retry-and-template-bugs`); this triggers `pr-pts` |
| Suite finishes in 4–5 min | AE narrates while the workflow runs; demo lead returns to the dashboard for the green session |

## Timing

| Phase | Wall clock |
| --- | --- |
| Maven build + Spring start | ~1 min |
| Full test suite (all ~100 tests) | 10–13 min |
| PTS subset run (target 50%) | 4–6 min |
| Full nightly-full workflow end-to-end | 11–14 min |
| Full pr-pts workflow end-to-end | 5–7 min |

The integration tests use a `SimulatedLatency` helper that's tunable via
the `latency.scale` system property. For local dev — when you don't want
to sit through the full suite — run with `-Dlatency.scale=0.1` to compress
sleeps 10×.

## If the demo wobbles

| Symptom | Mitigation |
| --- | --- |
| `pr-pts` run goes past 7 min | GitHub Actions queue is slow; demo lead falls back to a recorded green session in the workspace |
| PTS subset misses the failing tests | Re-trigger `nightly-full` to refresh the model with the latest fail set, then re-push the fix branch |
| Smart Tests CLI upload errors | Workspace token rotated; refresh `LAUNCHABLE_TOKEN` in repo secrets |
| Live build fails to compile | Run `./mvnw -B test-compile` locally before the demo to catch JDK/dependency issues early |

## Useful commands

```
# Full suite locally (slow — 10-15 min)
./mvnw test

# Unit tests only (fast)
./mvnw test -Dtest='*UnitTest'

# Integration only, sped up 10×
./mvnw test -Dtest='*IntegrationTest' -Dlatency.scale=0.1

# Inspect what PTS would select (requires `launchable` CLI auth'd locally)
./mvnw test-compile
launchable subset --target 50% maven src/test/java
```
