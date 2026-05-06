#!/usr/bin/env bash
# Initialises the demo repo: creates lived-in commit history on `main`
# and stages a `fix/retry-and-template-bugs` branch with both bugs fixed.
#
# Run once after cloning your fork (or from the delivered folder after
# `rm -rf .git`):
#
#   cd notifications-api
#   ./.demo/setup-demo.sh
#
# After this completes:
#   - `main` has the 10 failing tests (the nightly-full run will be red)
#   - `fix/retry-and-template-bugs` has the fixes; pushing it triggers
#     the PR-PTS workflow (the redemptive run in the demo)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

if [ -d .git ]; then
    echo "✗ .git already exists. Run from a fresh clone or delete .git first."
    exit 1
fi

# Verify the fixes are present before we start — bailing here is much
# easier than discovering the fix branch is empty later.
for f in .demo/fixes/RetryPolicy.java \
         .demo/fixes/TemplateEngine.java \
         .demo/fixes/CHANGELOG.md; do
    if [ ! -f "$f" ]; then
        echo "✗ missing fix file: $f"
        exit 1
    fi
done

git init -q -b main
git config user.email "demo@cloudbees.com"
git config user.name  "Demo Developer"

stage_and_commit () {
    local date="$1"; shift
    local message="$1"; shift
    git add "$@"
    GIT_AUTHOR_DATE="$date" GIT_COMMITTER_DATE="$date" \
        git commit -q -m "$message"
}

# Build a believable history. Order matters — files need to exist when
# referenced, and the bug-introducing commits sit in the middle so the
# nightly red doesn't look like it's been broken forever.

stage_and_commit "2026-02-25T09:00:00" \
    "chore: scaffold notifications-api repo" \
    README.md CHANGELOG.md CONTRIBUTING.md .gitignore pom.xml .github

stage_and_commit "2026-02-26T11:00:00" \
    "feat: bootstrap Spring Boot application and config" \
    src/main/java/com/cloudbees/demo/notifications/NotificationsApiApplication.java \
    src/main/java/com/cloudbees/demo/notifications/config/NotificationsProperties.java \
    src/main/resources/application.yml

stage_and_commit "2026-02-26T15:30:00" \
    "feat: core notification model, tenants, templates" \
    src/main/java/com/cloudbees/demo/notifications/core/ \
    src/main/java/com/cloudbees/demo/notifications/templates/Template.java \
    src/main/java/com/cloudbees/demo/notifications/templates/TemplateNotFoundException.java \
    src/main/java/com/cloudbees/demo/notifications/templates/TemplateRenderException.java \
    src/main/java/com/cloudbees/demo/notifications/templates/TemplateRepository.java \
    src/main/java/com/cloudbees/demo/notifications/tenants/ \
    src/main/java/com/cloudbees/demo/notifications/util/

stage_and_commit "2026-03-10T10:00:00" \
    "feat: email/sms/webhook channels and delivery tracker" \
    src/main/java/com/cloudbees/demo/notifications/channels/ \
    src/main/java/com/cloudbees/demo/notifications/delivery/DeliveryAttempt.java \
    src/main/java/com/cloudbees/demo/notifications/delivery/DeliveryRecord.java \
    src/main/java/com/cloudbees/demo/notifications/delivery/DeliveryTracker.java

stage_and_commit "2026-03-19T14:00:00" \
    "feat: retry policy, audit log, NotificationService orchestrator" \
    src/main/java/com/cloudbees/demo/notifications/delivery/RetryPolicy.java \
    src/main/java/com/cloudbees/demo/notifications/audit/ \
    src/main/java/com/cloudbees/demo/notifications/core/NotificationService.java

stage_and_commit "2026-04-02T09:30:00" \
    "feat: REST API and exception handling" \
    src/main/java/com/cloudbees/demo/notifications/api/

stage_and_commit "2026-04-08T11:00:00" \
    "test: unit test coverage across modules" \
    src/test/java/com/cloudbees/demo/notifications/templates/TemplateEngineUnitTest.java \
    src/test/java/com/cloudbees/demo/notifications/templates/TemplateRepositoryUnitTest.java \
    src/test/java/com/cloudbees/demo/notifications/audit/ \
    src/test/java/com/cloudbees/demo/notifications/channels/ \
    src/test/java/com/cloudbees/demo/notifications/delivery/DeliveryTrackerUnitTest.java \
    src/test/java/com/cloudbees/demo/notifications/delivery/RetryPolicyUnitTest.java \
    src/test/java/com/cloudbees/demo/notifications/tenants/ \
    src/test/java/com/cloudbees/demo/notifications/util/ \
    src/test/java/com/cloudbees/demo/notifications/core/

stage_and_commit "2026-04-22T15:00:00" \
    "test: integration coverage for delivery, retries, rate limits" \
    src/test/java/com/cloudbees/demo/notifications/integration/

# Anything left (workflows, .demo helpers, runbook, etc.)
git add -A
GIT_AUTHOR_DATE="2026-05-06T17:00:00" GIT_COMMITTER_DATE="2026-05-06T17:00:00" \
    git commit -q -m "ci: nightly-full and pr-pts workflows" || true

echo "✓ main is set up with $(git rev-list --count main) commits."

# --- fix branch ---------------------------------------------------------
# Straight file replacement — no patches, no fuzzy matching. The fixed
# versions live in .demo/fixes/ and overwrite the buggy ones on this branch.
git checkout -q -b fix/retry-and-template-bugs

cp .demo/fixes/RetryPolicy.java \
   src/main/java/com/cloudbees/demo/notifications/delivery/RetryPolicy.java

cp .demo/fixes/TemplateEngine.java \
   src/main/java/com/cloudbees/demo/notifications/templates/TemplateEngine.java

cp .demo/fixes/CHANGELOG.md CHANGELOG.md

# Sanity check — the fix branch MUST differ from main on these three files.
if git diff --quiet main -- \
        src/main/java/com/cloudbees/demo/notifications/delivery/RetryPolicy.java \
        src/main/java/com/cloudbees/demo/notifications/templates/TemplateEngine.java \
        CHANGELOG.md; then
    echo "✗ fix branch has no changes vs main — something went wrong copying fixes."
    exit 1
fi

git add src/main/java/com/cloudbees/demo/notifications/delivery/RetryPolicy.java \
        src/main/java/com/cloudbees/demo/notifications/templates/TemplateEngine.java \
        CHANGELOG.md
GIT_AUTHOR_DATE="2026-05-11T09:15:00" GIT_COMMITTER_DATE="2026-05-11T09:15:00" \
    git commit -q -m "fix(delivery,templates): null-safe RetryPolicy + restore underscore variables

Two issues from the nightly run:
- RetryPolicy NPE'd for tenants without a per-tenant DeliveryConfig
  override (the rollout is gradual — see CHANGELOG).
- TemplateEngine's variable regex was tightened in v1.4.0 and lost
  underscore support; templates using {{order_id}} etc. no longer
  rendered.

Both clusters of failing tests are fixed by this change."

git checkout -q main
echo "✓ fix/retry-and-template-bugs is set up."
echo "  Diff vs main: $(git diff --shortstat main fix/retry-and-template-bugs)"
echo
echo "Next steps:"
echo "  1. Push both branches to your GitHub fork."
echo "  2. Trigger the 'nightly-full' workflow once (manual dispatch) so"
echo "     your Smart Tests workspace has the failed session ready."
echo "  3. When ready to demo, push 'fix/retry-and-template-bugs' to"
echo "     trigger the 'pr-pts' workflow."
