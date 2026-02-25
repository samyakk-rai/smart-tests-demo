# Contributing

Thanks for working on notifications-api. Quick guide so PRs land smoothly.

## Local dev

Java 17 and Maven (or use the wrapper `./mvnw`).

```
./mvnw clean install
./mvnw spring-boot:run
```

## Tests

The full suite runs ~10–15 minutes. CI runs the full suite nightly and a
predictive-test-selection (PTS) subset on every PR — see
`.github/workflows/`.

When adding tests, place fast unit tests next to the production code they
exercise. Slower integration tests go under `src/test/java/.../integration/`
and should use the `IntegrationTest` JUnit tag.

## Code style

We follow standard Spring conventions. Run `./mvnw spotless:apply` before
pushing.

## Pull requests

- One feature/fix per PR
- Reference an issue or RFC where possible
- Keep CHANGELOG.md up to date
